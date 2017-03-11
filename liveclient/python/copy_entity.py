#!/usr/bin/env python
#coding: UTF-8

import os
import shutil

# 待拷贝资源的所在路径
org_res_path = "/Users/yangli/Development/huyu/walilive/app/src/main/res"
# 资源拷贝的目标路径
dst_res_path = "../src/main/res"

if not os.path.exists(org_res_path):
    print "error: org res path not found, please modify org_res_path in " \
            + os.path.basename(__file__)
    exit()

if not os.path.exists(dst_res_path):
    print "error: dst res path not found, please modify dst_res_path in " \
          + os.path.basename(__file__)
    exit()

class CopyDrawableRes:
    ensureItem = False

    def __init__(self, ensureItem):
        self.ensureItem = ensureItem
        pass

    def __del__(self):
        pass

    def doCopy(self, itemList):
        print "copy drawable resource for: " + "".join(itemList)
        if not itemList or len(itemList) == 0:
            return
        for srcItem in itemList:
            print "copy drawable-default " + srcItem
            ret = self.__doCopy("/drawable/", srcItem)
            if not ret:
                print "copy drawable-xxhdpi " + srcItem
                self.__doCopy("/drawable-xxhdpi/", srcItem)
        pass

    def __doCopy(self, resPath, resItem):
        srcPath = org_res_path + resPath
        dstPath = dst_res_path + resPath
        resFile = ""
        for fileName in os.listdir(srcPath):
            (name, ext) = os.path.splitext(fileName)
            if name == resItem:
                resFile = fileName
                break

        if not resFile or not resFile.strip():
            print 'warning: res not found in src path'
            return False

        srcFile = os.path.join(srcPath, resFile)
        dstFile = os.path.join(dstPath, resFile)

        if not os.path.isfile(srcFile):
            print 'warning: cannot copy path, can only copy file'
            return False

        if os.path.exists(dstFile):
            print 'warning: res already exists in dst path'
            return False

        shutil.copy (srcFile, dstFile)

        if self.ensureItem and resFile.lower().endswith(".xml"):
            self.__ensureItem(dstFile)

        return True
        pass

    def __ensureItem(self, xmlFile):
        # TODO implement this func
        pass

# 字符串资源拷贝
class CopyStringRes:
    addExtraLine = False # 是否字符串前增加空行

    def __init__(self, addExtraLine):
        self.addExtraLine = addExtraLine
        pass

    def __del__(self):
        pass

    def doCopy(self, itemList):
        print "copy string resource for: " + "".join(itemList)
        if not itemList or len(itemList) == 0:
            return
        print "copy default"
        self.__doCopy("/values/strings.xml", itemList)
        print "copy zh-rCN"
        self.__doCopy("/values-zh-rCN/strings.xml", itemList)
        print "copy zh-rTW"
        self.__doCopy("/values-zh-rTW/strings.xml", itemList)
        pass

    def __doCopy(self, resPath, resList):
        srcFile = org_res_path + resPath
        dstFile = dst_res_path + resPath
        # 读取原始数据
        srcData = ""
        input = open(srcFile)
        try:
            srcData = input.read()
        finally:
            input.close()

        # 读取目标数据
        out = open(dstFile, "r")
        try:
            dstData = out.read()
        finally:
            out.close()

        out = open(dstFile, "w")

        # 定位在文件末尾的写入位置
        dstPos = dstData.rfind("</resources>")
        if dstPos == -1:
            print "warning: cannot find </resources>"
            return

        # 依次处理每个待处理的项
        for item in resList:
            pos = dstData.find('<string name="' + item + '">')
            if pos != -1:
                # 目标中已有该项，则跳过不处理
                print "warning: item " + item + " already exists in dst"
                continue
            start = srcData.find('<string name="' + item + '">')
            if start == -1:
                print "warning: cannot find item " + item + " in src"
                continue
            end = srcData.find('</string>', start)
            content = '    ' + srcData[start:(end + len('</string>'))] + '\n'
            if self.addExtraLine: # 增加空行
                content = '\n' + content
                pass
            # print 'content: "' + content + '"\nwrite before: "' + dstData[dstPos:] + '"'
            dstData = dstData[:dstPos] + content + dstData[dstPos:]
            dstPos += len(content)
            pass

        # 写入新的目标数据
        try:
            out.write(dstData)
        finally:
            out.close()

        pass