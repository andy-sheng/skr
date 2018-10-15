#!/usr/bin/env python
#coding: UTF-8

import sys

from base.package_parser import *
from copy_base import *
from local_setting import *

curr_exec_path = os.path.abspath('.')
package_info = parse_package_info(
    curr_exec_path, None, None, None, None)
if not package_info:
    print "error: parse package info failed, please run this script under an android studio project."
    exit()
else:
    print "info: package info is: " + str(package_info)

# 资源拷贝的目标路径
dst_res_path = os.path.join(package_info.get_src_path(), 'res')
if not os.path.exists(dst_res_path):
    print "error: dst_res_path %s not exists" %dst_res_path
    exit()

# 获取类名
count = len(sys.argv)
if count <= 1:
    print "error: command argument is required!"
    print process.__doc__
    exit()

print 'org_res_path=%s dst_res_path=%s' %(org_res_path, dst_res_path)
process(sys.argv, count, org_res_path, dst_res_path)