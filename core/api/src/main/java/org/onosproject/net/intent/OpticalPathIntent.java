/*
 * Copyright 2014 Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.net.intent;

import java.util.Collection;

import org.onosproject.core.ApplicationId;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Link;
import org.onosproject.net.Path;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Preconditions.checkNotNull;

public final class OpticalPathIntent extends Intent {

    private final ConnectPoint src;
    private final ConnectPoint dst;
    private final Path path;


    private OpticalPathIntent(ApplicationId appId,
                              Key key,
                              ConnectPoint src,
                              ConnectPoint dst,
                              Path path,
                              int priority) {
        super(appId,
                key,
                ImmutableSet.copyOf(path.links()),
                priority);
        this.src = checkNotNull(src);
        this.dst = checkNotNull(dst);
        this.path = checkNotNull(path);
    }

    protected OpticalPathIntent() {
        this.src = null;
        this.dst = null;
        this.path = null;
    }

    /**
     * Returns a new optical connectivity intent builder.
     *
     * @return host to host intent builder
     */
    public static Builder builder() {
        return new Builder();
    }


    /**
     * Builder for optical path intents.
     */
    public static class Builder extends Intent.Builder {
        private ConnectPoint src;
        private ConnectPoint dst;
        private Path path;
        Key key;

        @Override
        public Builder appId(ApplicationId appId) {
            return (Builder) super.appId(appId);
        }

        @Override
        public Builder key(Key key) {
            return (Builder) super.key(key);
        }

        @Override
        public Builder priority(int priority) {
            return (Builder) super.priority(priority);
        }

        /**
         * Sets the source for the intent that will be built.
         *
         * @param src source to use for built intent
         * @return this builder
         */
        public Builder src(ConnectPoint src) {
            this.src = src;
            return this;
        }

        /**
         * Sets the destination for the intent that will be built.
         *
         * @param dst dest to use for built intent
         * @return this builder
         */
        public Builder dst(ConnectPoint dst) {
            this.dst = dst;
            return this;
        }

        /**
         * Sets the path for the intent that will be built.
         *
         * @param path path to use for built intent
         * @return this builder
         */
        public Builder path(Path path) {
            this.path = path;
            return this;
        }

        /**
         * Builds an optical path intent from the accumulated parameters.
         *
         * @return optical path intent
         */
        public OpticalPathIntent build() {

            return new OpticalPathIntent(
                    appId,
                    key,
                    src,
                    dst,
                    path,
                    priority
            );
        }
    }


    public ConnectPoint src() {
        return src;
    }

    public ConnectPoint dst() {
        return dst;
    }

    public Path path() {
        return path;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass())
                .add("id", id())
                .add("appId", appId())
                .add("key", key())
                .add("resources", resources())
                .add("ingressPort", src)
                .add("egressPort", dst)
                .add("path", path)
                .toString();
    }


    public Collection<Link> requiredLinks() {
        return path.links();
    }
}
