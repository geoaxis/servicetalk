/**
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
package io.servicetalk.http.netty;

import io.servicetalk.http.api.HttpCookies;
import io.servicetalk.http.api.HttpHeaders;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import static io.servicetalk.http.netty.HeaderUtils.DEFAULT_HEADER_FILTER;
import static io.servicetalk.http.netty.HttpHeaderNames.COOKIE;
import static io.servicetalk.http.netty.HttpHeaderNames.SET_COOKIE;
import static java.util.Objects.requireNonNull;

final class NettyToServiceTalkHttpHeaders implements HttpHeaders {
    private final io.netty.handler.codec.http.HttpHeaders nettyHeaders;

    NettyToServiceTalkHttpHeaders(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        this.nettyHeaders = requireNonNull(nettyHeaders);
    }

    @Override
    public boolean contains(CharSequence name, CharSequence value) {
        return nettyHeaders.contains(name, value, false);
    }

    @Override
    public boolean contains(CharSequence name, CharSequence value, boolean caseInsensitive) {
        return nettyHeaders.contains(name, value, caseInsensitive);
    }

    @Override
    public HttpCookies parseCookies(boolean validateContent) {
        return new DefaultHttpCookies(this, COOKIE, validateContent);
    }

    @Override
    public HttpCookies parseSetCookies(boolean validateContent) {
        return new DefaultHttpCookies(this, SET_COOKIE, validateContent);
    }

    @Nullable
    @Override
    public CharSequence get(CharSequence name) {
        return nettyHeaders.get(name);
    }

    @Nullable
    @Override
    public CharSequence getAndRemove(CharSequence name) {
        CharSequence value = nettyHeaders.get(name);
        if (value != null) {
            nettyHeaders.remove(name);
        }
        return value;
    }

    @Override
    public Iterator<? extends CharSequence> getAll(CharSequence name) {
        return nettyHeaders.valueCharSequenceIterator(name);
    }

    @Override
    public int size() {
        return nettyHeaders.size();
    }

    @Override
    public boolean isEmpty() {
        return nettyHeaders.isEmpty();
    }

    @Override
    public Set<? extends CharSequence> getNames() {
        return nettyHeaders.names();
    }

    @Override
    public HttpHeaders add(CharSequence name, CharSequence value) {
        nettyHeaders.add(name, value);
        return this;
    }

    @Override
    public HttpHeaders add(CharSequence name, Iterable<? extends CharSequence> values) {
        nettyHeaders.add(name, values);
        return this;
    }

    @Override
    public HttpHeaders add(CharSequence name, CharSequence... values) {
        nettyHeaders.add(name, new ArrayIterable<>(values));
        return this;
    }

    @Override
    public HttpHeaders add(HttpHeaders headers) {
        if (headers == this) {
            throw new IllegalArgumentException("can't add to itself");
        }
        if (headers instanceof NettyToServiceTalkHttpHeaders) {
            nettyHeaders.add(((NettyToServiceTalkHttpHeaders) headers).nettyHeaders);
        } else {
            for (Map.Entry<? extends CharSequence, ? extends CharSequence> entry : headers) {
                nettyHeaders.add(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public HttpHeaders set(CharSequence name, CharSequence value) {
        nettyHeaders.set(name, value);
        return this;
    }

    @Override
    public HttpHeaders set(CharSequence name, Iterable<? extends CharSequence> values) {
        nettyHeaders.set(name, values);
        return this;
    }

    @Override
    public HttpHeaders set(CharSequence name, CharSequence... values) {
        nettyHeaders.set(name, new ArrayIterable<>(values));
        return this;
    }

    @Override
    public boolean remove(CharSequence name) {
        return getAndRemove(name) != null;
    }

    @Override
    public HttpHeaders clear() {
        nettyHeaders.clear();
        return this;
    }

    @Override
    public Iterator<Map.Entry<CharSequence, CharSequence>> iterator() {
        return nettyHeaders.iteratorCharSequence();
    }

    @Override
    public HttpHeaders copy() {
        return new NettyToServiceTalkHttpHeaders(nettyHeaders.copy());
    }

    @Override
    public String toString(BiFunction<? super CharSequence, ? super CharSequence, CharSequence> filter) {
        return HeaderUtils.toString(this, filter);
    }

    @Override
    public String toString() {
        return toString(DEFAULT_HEADER_FILTER);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HttpHeaders)) {
            return false;
        }

        return HeaderUtils.equals(this, (HttpHeaders) o);
    }

    @Override
    public int hashCode() {
        return HeaderUtils.hashCode(this);
    }
}
