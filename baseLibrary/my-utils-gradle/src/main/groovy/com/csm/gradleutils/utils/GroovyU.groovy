package com.csm.gradleutils.utils

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.internal.scope.GlobalScope
import org.gradle.api.Project;

 class GroovyU {
    public static GlobalScope getGlobalScope(Project project){
         def appPlugin = project.plugins.getPlugin(AppPlugin)
         def taskManager = BasePlugin.metaClass.getProperty(appPlugin,"taskManager")
         taskManager.globalScope
     }
}
