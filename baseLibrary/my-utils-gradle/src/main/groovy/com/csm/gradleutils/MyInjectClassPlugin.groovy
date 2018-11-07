package com.csm.gradleutils

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.csm.gradleutils.config.InjectConfig
import com.csm.gradleutils.processor.InjectGifProcessor
import com.csm.gradleutils.processor.TimeStatisticsProcessor
import com.csm.gradleutils.transform.ReClassTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.invocation.Gradle

import java.util.regex.Matcher
import java.util.regex.Pattern

public class MyInjectClassPlugin implements Plugin<Project> {



    void apply(Project project) {
        System.out.println("========================");
        System.out.println("Hello MethodStatisticsPlugin!");
        System.out.println("========================");

        project.extensions.create("injectConfig", InjectConfig.class)

        // 新建一个task
        project.task('readExtension') << {
            def injectConfig = project['injectConfig']
            println "injectConfig.injectMethodStatictis: " + injectConfig.injectMethodStatictis
        }

// 仅处理application合包
        if (project.plugins.hasPlugin(AppPlugin.class)) {
            /**
             * android{}、compileSdkVersion、defaultConfig {}* 这些属性里面的值就是通过 Extension 被Android的Gradle插件读取到的。
             */
            def android = project.extensions.getByType(AppExtension.class)
            ReClassTransform reClassTransform = new ReClassTransform(project)
            android.registerTransform(reClassTransform)

        }
    }
}
