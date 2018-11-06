/*
 * Copyright (C) 2005-2017 Qihoo 360 Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed To in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.csm.gradleutils.utils;

import java.util.HashMap;

/**
 * @author RePlugin Team
 */
public class CommonData {

    /**
     * 保存类文件名和 class 文件路径的关系
     * 如
     * com.zq.live.MainActivity  --> /Users/chengsimin/dev/livesdk/livesdk/app/build/intermediates/transforms/desugar/channel_mishop/debug/7
     */
    public static HashMap<String, String> sClassAndPath = new HashMap<>();


    public static void putClassAndPath(String className, String classFilePath) {
        sClassAndPath.put(className, classFilePath);
    }

    public static String getClassPath(String className) {
        return sClassAndPath.get(className);
    }
}
