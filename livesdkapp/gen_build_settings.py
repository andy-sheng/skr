#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys
f = open('./src/main/AndroidManifest.xml', 'r')
s = ''
for line in f:
    line = line.replace("<category android:name=\"android.intent.category.LAUNCHER\" />","")
    s += line
f.close()
f = open('./src/main/AndroidManifest.xml', 'w')
f.write(s)
