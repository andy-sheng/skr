#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys

f = open('../buildsystem/dependencies.gradle', 'r')
s = ''
for line in f:
    line = line.replace("buildFlavorType = 'livesdkapp'", "buildFlavorType = 'livesdkaar'")
    s += line
print s
f.close()
f = open('../buildsystem/dependencies.gradle', 'w')
f.write(s)
f.close()
