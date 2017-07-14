#!/usr/bin/env python
# coding: UTF-8

from local_setting import *

# 图片资源拷贝
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

        # if not resFile or not resFile.strip():
        #    srcPath = org_res_path1 + resPath
        #    for fileName in os.listdir(srcPath):
        #        (name, ext) = os.path.splitext(fileName)
        #        if name == resItem:
        #            resFile = fileName
        #            break

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
            if self.ensureItem and resFile.lower().endswith(".xml"):
                self.__ensureItem(dstFile)
                return True
            return False

        shutil.copy(srcFile, dstFile)

        if self.ensureItem and resFile.lower().endswith(".xml"):
            self.__ensureItem(dstFile)

        return True
        pass

    def __ensureItem(self, xmlFile):
        print "\nensure item for: " + xmlFile
        content = ""
        input = open(xmlFile, "r")
        try:
            content = input.read()
        finally:
            input.close()

        subItemList = []
        start = 0
        while True:
            start = content.find('"@drawable/', start)
            if start == -1:
                break
            end = content.find('"', start + 1)
            if end == -1:
                break
            subItemList.append(content[(start + len('"@drawable/')):end])
            start = end
            pass

        if subItemList and len(subItemList) > 0:
            self.doCopy(subItemList)

        pass


# 字符串资源拷贝
class CopyValueRes:
    addExtraLine = False  # 是否字符串前增加空行
    subPath = 'strings'  # 资源路径，默认为strings
    subType = 'string'  # 资源类别，默认为string

    def __init__(self, addExtraLine, subPath, subType):
        self.addExtraLine = addExtraLine
        if subPath and subPath.strip():
            self.subPath = subPath
        if subType and subType.strip():
            self.subType = subType
        pass

    def __del__(self):
        pass

    def doCopy(self, itemList):
        print "\ncopy value resource in " + self.subPath + " of " + self.subType + " for: " + "".join(itemList)
        if not itemList or len(itemList) == 0:
            return
        print "copy default"
        self.__doCopy('/values/' + self.subPath + '.xml', itemList)
        print "copy zh-rCN"
        self.__doCopy('/values-zh-rCN/' + self.subPath + '.xml', itemList)
        print "copy zh-rTW"
        self.__doCopy('/values-zh-rTW/' + self.subPath + '.xml', itemList)
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
        dstPos = dstData.rfind('</resources>')
        if dstPos == -1:
            print "warning: cannot find </resources>"
            return

        # 依次处理每个待处理的项
        for item in resList:
            header = '<' + self.subType + ' name="' + item + '"'
            tailor = '</' + self.subType + '>'
            pos = dstData.find(header)
            if pos != -1:
                # 目标中已有该项，则跳过不处理
                print "warning: item " + item + " already exists in dst"
                continue
            start = srcData.find(header)
            if start == -1:
                print "warning: cannot find item " + item + " in src"
                continue
            end = srcData.find(tailor, start)
            content = '    ' + srcData[start:(end + len(tailor))] + '\n'
            if self.addExtraLine:  # 增加空行
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


# layout资源拷贝
class CopyLayoutRes:
    def __init__(self):
        pass

    def __del__(self):
        pass

    def doCopy(self, itemList):
        print "copy layout for: ".join(itemList)
        if not itemList or len(itemList) == 0:
            return
        for srcItem in itemList:
            print "copy layout " + srcItem
            ret = self.__doCopy("/layout/", srcItem)
            if not ret:
                print "copy /layout/ " + srcItem
                self.__doCopy("/layout/", srcItem)
        pass

    def __doCopy(self, resPath, fileName):
        srcFile = org_res_path + resPath + fileName + '.xml'
        print 'srcFile=' + srcFile
        dstPath = dst_res_path + resPath
        print 'distFile=' + dstPath

        if not os.path.exists(dstPath):
            os.makedirs(dstPath)

        try:
            shutil.copy(srcFile, dstPath)
        # eg. src and dest are the same file
        except shutil.Error as e:
            print('Error: 1 %s' % e)
        # eg. source or destination doesn't exist
        except IOError as e:
            print('Error: 2 %s' % e.strerror)
        pass
