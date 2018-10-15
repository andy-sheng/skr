/*
 * Copyright 2017 JessYan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.common.base;

import java.io.File;
public class GlobalParams {
    private File mCacheFile;

    private GlobalParams(Builder builder) {
        this.mCacheFile = builder.cacheFile;
    }

    public static Builder builder() {
        return new Builder();
    }



    public static final class Builder {
        private File cacheFile;

        private Builder() {
        }

        public GlobalParams build() {
            return new GlobalParams(this);
        }


    }


}
