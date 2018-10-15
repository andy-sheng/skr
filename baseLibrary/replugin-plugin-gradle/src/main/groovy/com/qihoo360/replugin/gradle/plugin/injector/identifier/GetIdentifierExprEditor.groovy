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

package com.qihoo360.replugin.gradle.plugin.injector.identifier

import com.qihoo360.replugin.gradle.plugin.inner.CommonData
import javassist.CannotCompileException
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

/**
 * @author RePlugin Team
 */
public class GetIdentifierExprEditor extends ExprEditor {

    public def filePath

    @Override
    void edit(MethodCall m) throws CannotCompileException {
        String clsName = m.getClassName()
        String methodName = m.getMethodName()

        if (clsName.equalsIgnoreCase('android.content.res.Resources')) {
            if (methodName == 'getIdentifier') {
                /**
                 * 这里修复得不到android状态栏高度
                 * int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
                 * replace 会直接在 getIdentifier 前加上 if 里的代码
                 */
                m.replace('{ ' +
                        'if(!$3.equals("android")){ ' +
                        '$3 = \"' + CommonData.appPackage + '\"; ' +
                        '}' +
                        '$_ = $proceed($$);' +
                        ' }')
                println " GetIdentifierCall => ${filePath} ${methodName}():${m.lineNumber}"
            }
        }
    }
}
