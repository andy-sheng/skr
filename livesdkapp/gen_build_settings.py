#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys

f = open('../common/src/main/java/com/base/utils/Constants.java', 'r')
s = ''
channlName = sys.argv[1];
if (channlName == 'DEFAULT_1'):
    channlName = 'DEFAULT'
for line in f:
  line = line.replace("@SHIP.TO.2A2FE0D7@", channlName)
  s += line
f.close()
#修改了constant的包路径
f = open('../common/src/main/java/com/base/utils/Constants.java', 'w')
f.write(s)
f.close()

if (cmp(channlName,'DEBUG') != 0):
    f = open('./src/main/AndroidManifest.xml', 'r')
    s = ''
    for line in f:
        line = line.replace("<category android:name=\"android.intent.category.LAUNCHER\" />","")
        s += line
    f.close()
    f = open('./src/main/AndroidManifest.xml', 'w')
    f.write(s)
    f.close()

f = open('../buildsystem/dependencies.gradle', 'r')
s = ''
for line in f:
    line = line.replace("buildFlavorType = 'livesdkaar'", "buildFlavorType = 'livesdkapp'")
    s += line
f.close()
f = open('../buildsystem/dependencies.gradle', 'w')
f.write(s)
f.close()

if(sys.argv[1] == 'debugmi'):
    f = open('./build.gradle', 'r')
    s = ''
    for line in f:
      line = line.replace("//    debugCompile project(':debugmi')", "compile project(':debugmi')")
      s += line
    f.close()
    f = open('./build.gradle', 'w')
    f.write(s)
    f.close()
