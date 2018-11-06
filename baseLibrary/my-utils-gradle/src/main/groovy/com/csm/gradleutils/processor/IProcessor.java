package com.csm.gradleutils.processor;

import javassist.ClassPool;

public interface IProcessor {
    void process(String dirPath, ClassPool classPool);
}
