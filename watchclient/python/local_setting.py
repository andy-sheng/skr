#!/usr/bin/env python
# coding: UTF-8

import os
import shutil

# 待拷贝资源的所在路径
org_res_path = "/home/wmj/xiaomi/walilive/app/src/main/res"

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
