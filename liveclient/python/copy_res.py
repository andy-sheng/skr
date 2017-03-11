#!/usr/bin/env python
#coding: UTF-8

import sys

from copy_entity import *

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
        print '-s [string source] [string source] ... (note: if use -S, a new empty line will be inserted before each item)'
        print '-I [drawable source] [drawable source] ...'
        print '-D [dimen source] [dimen source] ...'
        print '-L [layout source] [layout source] ...'
    elif command.lower() == "-s":
        copier = CopyStringRes(command == "-S")
        resList = []
        while i < count:
            item = sys.argv[i]
            if item[0] == "-":
                break
            resList.append(item)
            i += 1
        copier.doCopy(resList)