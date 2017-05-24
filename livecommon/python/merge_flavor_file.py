#!/usr/bin/env python
#coding: UTF-8

import os
import shutil

def readFile(inputFile):
    content = ""
    input = open(inputFile, "r")
    try:
        content = input.read()
    finally:
        input.close()
    return content
pass

def mergePath(inPath1, inPath2, outPath):
    allPath = os.listdir(inPath1)
    for path in allPath:
        newPath1 = os.path.join(inPath1, path)
        newPath2 = os.path.join(inPath2, path)
        if os.path.isfile(newPath1) and os.path.isfile(newPath2):
            content1 = readFile(newPath1)
            content2 = readFile(newPath2)
            if not content1 or not content2 or content1 != content2:
                print "note: content not identical between " + newPath1 + " and " + newPath2
                continue
            if not os.path.exists(outPath):
                os.makedirs(outPath)
            shutil.copy(newPath1, outPath)
            os.remove(newPath1)
            os.remove(newPath2)
            print "info: copy file from " + newPath1 + ", to " + outPath
        elif os.path.isdir(newPath1) and os.path.isdir(newPath2):
            newOutPath = os.path.join(outPath, path)
            mergePath(newPath1, newPath2, newOutPath)
    pass
    if len(os.listdir(inPath1)) == 0:
        os.rmdir(inPath1)
        print "info: remove path " + inPath1
    if len(os.listdir(inPath2)) == 0:
        os.rmdir(inPath2)
        print "info: remove path " + inPath2
pass

template = '../src/{name}/java/'
srcPath1 = template.format(name = 'livesdkaar')
srcPath2 = template.format(name = 'livesdkapp')
outPath = template.format(name = 'main')

mergePath(srcPath1, srcPath2, outPath)