package com.csm.gradleutils.processor;

import com.csm.gradleutils.utils.U;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;

public abstract class BaseProcessor implements IProcessor {
    @Override
    public void process(String dirPath, ClassPool classPool) {
        if(!new File(dirPath).exists()){
            return;
        }
        try {
            Files.walkFileTree(Paths.get(dirPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    U.print(0, "file" + file);
                    FileInputStream fileInputStream = null;
                    CtClass ctClass = null;
                    try {
                        File ff = file.toFile();
                        if (!ff.exists() || !file.toString().endsWith(".class")) {
                            return super.visitFile(file, attrs);
                        }
                        fileInputStream = new FileInputStream(ff);
                        ctClass = classPool.makeClass(fileInputStream);
                        if (ctClass.isFrozen()) {
                            ctClass.defrost();
                        }
                        U.print(0, " getName:" + ctClass.getName());
                        processClass(ctClass);
                        ctClass.writeFile(dirPath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (CannotCompileException e) {
                        e.printStackTrace();
                    } finally {
                        if (ctClass != null) {
                            ctClass.detach();
                        }
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void processClass(CtClass ctClass) throws CannotCompileException;
}
