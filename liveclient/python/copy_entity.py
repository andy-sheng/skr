#!/usr/bin/env python
#coding: UTF-8

# res路径
dst_res_path = "../src/main/res"
org_res_path = "/Users/yangli/Development/huyu/walilive/app/src/main/res"

class CopyStringRes:
    addExtraLine = False # 是否字符串前增加空行

    def __init__(self, addExtraLine):
        self.addExtraLine = addExtraLine
        pass

    def __del__(self):
        pass

    def doCopy(self, strList):
        print "copy string resource for: ".join(strList)
        if not strList or len(strList) == 0:
            return
        print "copy default"
        self.__doCopy("/values/strings.xml", "/values/strings.xml", strList)
        print "copy zh-rCN"
        self.__doCopy("/values-zh-rCN/strings.xml", "/values-zh-rCN/strings.xml", strList)
        print "copy zh-rTW"
        self.__doCopy("/values-zh-rTW/strings.xml", "/values-zh-rTW/strings.xml", strList)
        pass

    def __doCopy(self, srcFile, dstFile, strList):
        srcFile = org_res_path + srcFile
        dstFile = dst_res_path + dstFile
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
        for item in strList:
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