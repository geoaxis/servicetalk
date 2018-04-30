/*
 * Copyright © 2018 Apple Inc. and the ServiceTalk project authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicetalk.concurrent.api;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static java.util.Objects.requireNonNull;

final class DoAfterFinallyPublisher<T> extends AbstractSynchronousPublisherOperator<T, T> {

    private final Runnable runnable;

    DoAfterFinallyPublisher(Publisher<T> original, Runnable runnable, Executor executor) {
        super(original, executor);
        this.runnable = requireNonNull(runnable);
    }

    @Override
    public Subscriber<? super T> apply(Subscriber<? super T> subscriber) {
        return new DoAfterFinallyPublisherSubscriber<>(subscriber, runnable);
    }

    private static final class DoAfterFinallyPublisherSubscriber<T> implements Subscriber<T> {
        private final Subscriber<? super T> original;
        private final Runnable runnable;

        private static final AtomicIntegerFieldUpdater<DoAfterFinallyPublisherSubscriber> completeUpdater =
                AtomicIntegerFieldUpdater.newUpdater(DoAfterFinallyPublisherSubscriber.class, "complete");
        @SuppressWarnings("unused")
        private volatile int complete;

        DoAfterFinallyPublisherSubscriber(Subscriber<? super T> original, Runnable runnable) {
            this.original = original;
            this.runnable = runnable;
        }

        @Override
        public void onSubscribe(Subscription s) {
            original.onSubscribe(new Subscription() {
                @Override
                public void request(long n) {
                    s.request(n);
                }

                @Override
                public void cancel() {
                    try {
                        s.cancel();
                    } finally {
                        doAfterFinally();
                    }
                }
            });
        }

        @Override
        public void onNext(T t) {
            original.onNext(t);
        }

        @Override
        public void onComplete() {
            try {
                original.onComplete();
            } finally {
                doAfterFinally();
            }
        }

        @Override
        public void onError(Throwable cause) {
            try {
                original.onError(cause);
            } finally {
                doAfterFinally();
            }
        }

        private void doAfterFinally() {
            if (completeUpdater.compareAndSet(this, 0, 1)) {
                runnable.run();
            }
        }
    }
}
