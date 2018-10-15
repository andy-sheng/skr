#!/usr/bin/env python
#coding: UTF-8

import os

from base.creator_base import *
from base.utils import *
from base.template_info import *

root_package = root_package + '.data'
root_path = os.path.join(root_path, 'data')

# 生成Presenter
name1 = name + 'Presenter'
name2 = 'Object'
class_package = root_package
class_import = ''
template = TemplateInfo(class_package, class_import, name1, name2, command, user_name, user_date)
out_file = os.path.join(root_path, name1 + '.java')
check_path(os.path.dirname(out_file))
template.write_to_file(os.path.join(template_path, 'data', 'DataPresenter.java'), out_file)