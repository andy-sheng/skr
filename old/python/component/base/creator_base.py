#!/usr/bin/env python
#coding: UTF-8

import time
import getpass

from command_parser import *
from package_parser import *

# 脚本名称
command = os.path.basename(sys.argv[0])
# 用户名
user_name = getpass.getuser()
# 日期
user_date = time.strftime('%Y/%m/%d',time.localtime(time.time()))

__curr_exec_path = os.path.abspath('.')

command_info = parse_command_info(sys.argv)
if not command_info:
    print "waring: no command info found."
    exit()
elif not command_info.name:
    print "error: parse command info failed, component name should be specified."
    exit()
else:
    print "info: command info is: " + str(command_info)

package_info = parse_package_info(
    __curr_exec_path, command_info.module, command_info.source, command_info.language, command_info.package)
if not package_info:
    print "error: parse package info failed, please run this script under an android studio project."
    exit()
else:
    print "info: package info is: " + str(package_info)

# 包名
root_package = package_info.get_class_package()
# 文件目录
root_path = package_info.get_class_path()
# 名称
name = command_info.name
# 模版路径
__script_path = os.path.split(os.path.realpath(__file__))[0]
template_path = os.path.join(os.path.dirname(__script_path), 'template')