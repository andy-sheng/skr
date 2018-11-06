package com.csm.gradleutils.utils;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.TransformInput;
import com.android.build.gradle.internal.scope.GlobalScope;
import com.android.sdklib.IAndroidTarget;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import groovy.util.AntBuilder;

import static com.android.builder.model.AndroidProject.FD_INTERMEDIATES;

public class U {

    public final static String TAG = "程思敏 ";

    /**
     * 生成 ClassPool 使用的 ClassPath 集合，同时将要处理的 jar 写入 includeJars
     */
    public static List<String> getClassPaths(Project project, GlobalScope globalScope,
                                             Collection<TransformInput> inputs,
                                             Set<String> includeJars,
                                             Map<String, String> map) {
        List<String> classpathList = new ArrayList<>();

        // android.jar
        // 即 /Users/chengsimin/my_dev_utils/android-sdk-macosx-24.4/platforms/android-27/android.jar
        String androidJarPath = getAndroidJarPath(globalScope);
        print(0,"androidJarPath:" + androidJarPath);
        classpathList.add(androidJarPath);

        // 原始项目中引用的 classpathList
        List<String> l = getProjectClassPath(project, inputs, includeJars, map);
        classpathList.addAll(l);
        /**
         * 最后返回的是如
         * /Users/chengsimin/dev/livesdk/livesdk/app/build/intermediates/transforms/desugar/channel_default/debug/1
         * 这样的路径
         */
        return classpathList;
    }

    /**
     * 获取原始项目中的 ClassPath
     */
    private static List<String> getProjectClassPath(Project project,
                                                    Collection<TransformInput> inputs,
                                                    Set<String> includeJars, Map<String, String> map) {
        List<String> classPath = new ArrayList<>();

        ClassFileVisitor visitor = new ClassFileVisitor();

        // /Users/chengsimin/dev/livesdk/livesdk
        String projectDir = project.getRootDir().getAbsolutePath();
        print(0,"project.getRootDirPath:" + projectDir);

        for (TransformInput transformInput : inputs) {
            for (DirectoryInput directoryInput : transformInput.getDirectoryInputs()) {
                String dirPath = directoryInput.getFile().getAbsolutePath();
                classPath.add(dirPath);
                visitor.setBaseDir(dirPath);
                try {
                    Files.walkFileTree(Paths.get(dirPath), visitor);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (JarInput jarInput : transformInput.getJarInputs()) {
                File jar = jarInput.getFile();
                String jarPath = jar.getAbsolutePath();
                print(0,"jarPath:" + jarPath);
                if (!jarPath.contains(projectDir)) {
                    String jarZipDir = project.getBuildDir().getPath() +
                            File.separator + FD_INTERMEDIATES + File.separator + "exploded-aar" +
                            File.separator + Hashing.sha1().hashString(jarPath, Charsets.UTF_16LE).toString() + File.separator + "class";
                    print(0,"jarZipDir:" + jarZipDir);
                    if (unzip(jarPath, jarZipDir)) {
                        String jarZip = jarZipDir + ".jar";
                        includeJars.add(jarPath);
                        classPath.add(jarZipDir);
                        visitor.setBaseDir(jarZipDir);
                        try {
                            Files.walkFileTree(Paths.get(jarZipDir), visitor);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        map.put(jarPath, jarZip);
                    }
                } else {
                    //基本都是走这
                    includeJars.add(jarPath);
                    map.put(jarPath, jarPath);

                    /*
                    [unzip] Expanding:
                    jarPath: /Users/chengsimin/dev/livesdk/livesdk/app/build/intermediates/transforms/desugar/channel_default/debug/1.jar
                    into
                    jarZipDir: /Users/chengsimin/dev/livesdk/livesdk/app/build/intermediates/transforms/desugar/channel_default/debug/1

                    putClassAndPath:
                    com.facebook.fresco.animation.drawable.AnimatedDrawable2
                    -->
                    /Users/chengsimin/dev/livesdk/livesdk/app/build/intermediates/transforms/desugar/channel_default/debug/1
                    */
                    /* 将 jar 包解压，并将解压后的目录加入 classpath */
                    // println ">>> 解压Jar${jarPath}"
                    String jarZipDir = jar.getParent() + File.separatorChar + jar.getName().replace(".jar", "");
                    if (unzip(jarPath, jarZipDir)) {
                        classPath.add(jarZipDir);

                        visitor.setBaseDir(jarZipDir);
                        try {
                            Files.walkFileTree(Paths.get(jarZipDir), visitor);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // 删除 jar
                    try {
                        FileUtils.forceDelete(jar);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return classPath;
    }

    /**
     * 编译环境中 android.jar 的路径
     */
    public static String getAndroidJarPath(GlobalScope globalScope) {
        return globalScope.getAndroidBuilder().getTarget().getPath(IAndroidTarget.ANDROID_JAR);
    }


    /**
     * 解压 zipFilePath 到 目录 dirPath
     */
    public static boolean unzip(String zipFilePath, String dirPath) {
        // 若这个Zip包是空内容的（如引入了Bugly就会出现），则直接忽略
        if (isZipEmpty(zipFilePath)) {
            System.out.println(">>> Zip file is empty! Ignore");
            return false;
        }
        HashMap<String, String> m = new HashMap();
        m.put("src", zipFilePath);
        m.put("dest", dirPath);
        m.put("overwrite", "true");
        new AntBuilder().invokeMethod("unzip", m);
        return true;
    }

    /**
     * 压缩 dirPath 到 zipFilePath
     */
    public static void zipDir(String dirPath, String zipFilePath) {
        HashMap<String, String> m = new HashMap();
        m.put("destfile", zipFilePath);
        m.put("basedir", dirPath);
        new AntBuilder().invokeMethod("zip",m);
    }


    public static boolean isZipEmpty(String zipFilePath) {
        ZipFile z = null;
        try {
            z = new ZipFile(zipFilePath);
            return z.size() == 0;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                z.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public static void print(int level,String log){
        if(level>1){
            System.out.println(U.TAG+" "+log);
        }
    }
}
