#!/usr/bin/env python
#coding: UTF-8

import sys
import os

# res路径
dst_res_path = "../src/main/res";
org_res_path = "/Users/yangli/Development/huyu/walilive/app/src/main/res"

# 获取类名
count = len(sys.argv)
if count <= 1:
    print "error: bad argument, a component name is required!"
    exit()

i = 0
while i < count:
    command = sys.argv[i]
    i += 1
    if command.lower() == "-h" or command.lower() == "--help":
        print '-S [string source] ...'
        print '-I [drawable source] ...'
        print '-D [dimen source] ...'
        print '-L [layout source] ...'
    elif command == "-s":
        resList = []
        while i < count:
            item = sys.argv[i]
            if item[0] == "-":
                break
            resList.append(item)
            i += 1
    elif command = "-"

class CopyStringRes:

    def doCopy(self, strList):
        if not strList or len(strList) == 0:
            return
        __do_copy("/values/strings.xml", "/values/strings.xml", strList)
        __do_copy("/values-zh-rCN/strings.xml", "/values-zh-rCN/strings.xml", strList)
        __do_copy("/values-zh-rTW/strings.xml", "/values-zh-rTW/strings.xml", strList)

    def __do_copy(self, srcFile, dstFile, strList):
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
        out = open(outFile, "r+")
        try:
            dstData = out.read();
        finally:
            out.close()

        # 定位在文件末尾的写入位置
        dstPos = dstData.find("</resources>")
        if dstPos != -1:
            dstPos = len(dstData)

        # 依次处理每个待处理的项
        for item in strList:
            start = input.find('<string name="' + item + '">')
            if start == -1:
                continue
            end = input.find('</string>')
            content = input[start:(end + len('</string>'))]
            dstData = dstData[:dstPos] + '    ' + content + '\n' + dstData[dstPos:]

        # 写入新的目标数据
        try:
            out.write(dstData);
        finally:
            out.close();

        print "copy string resource from " + srcFile + " to " + dstFile + ":\n" + strList;