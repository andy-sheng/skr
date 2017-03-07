#!/usr/bin/env python
#coding: UTF-8

import sys
import os
import time
import getpass

# 获取类名
command = sys.argv[0]
if len(sys.argv) <= 1 or not sys.argv[1].strip():
    print "error: bad argument, a component name is required!"
    exit()
name = sys.argv[1]

# 获取用户名及日期
user_name = getpass.getuser()
user_date = time.strftime('%Y/%m/%d',time.localtime(time.time()))

def checkPath(path):
    if not path or not path.strip() or os.path.exists(path):
        return
    os.makedirs(path)
    pass

class TemplateInfo:
    PACKAGE = ""
    IMPORT = ""
    USER = ""
    DATE = ""
    NAME = ""
    COMMAND = ""

    def __init__(self, PACKAGE, IMPORT):
        global user_name, user_date
        self.PACKAGE = PACKAGE
        self.IMPORT = IMPORT
        self.USER = user_name
        self.DATE = user_date
        self.NAME = name
        self.COMMAND = command

    def __del__(self):
        pass

    def writeToFile(self, tmplFile, outFile):
        if os.path.exists(outFile):
            print "warning: " + outFile + " already existed!"
            return
        content = ""
        input = open(tmplFile);
        try:
            content = input.read();
        finally:
            input.close()

        content = content.replace("${PACKAGE}", self.PACKAGE) \
            .replace("${IMPORT}", self.IMPORT) \
            .replace("${USER}", self.USER) \
            .replace("${DATE}", self.DATE) \
            .replace("${NAME}", self.NAME) \
            .replace("${COMMAND}", self.COMMAND)

        out = open(outFile, "w")
        out.write(content);
        out.close();

        print "generate " + outFile + " done!"