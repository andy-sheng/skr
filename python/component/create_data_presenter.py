#!/usr/bin/env python
#coding: UTF-8

import os

from base.creator_base import *
from base.utils import *
from base.template_info import *

root_package = root_package + '.data'
root_path = os.path.join(root_path, 'data')

# 生成Presenter
class_package = root_package
class_import = ''
template = TemplateInfo(class_package, class_import, name, command, user_name, user_date)
out_file = os.path.join(root_path, name + 'Presenter.java')
check_path(root_path)
template.write_to_file(os.path.join(template_path, 'data', 'DataPresenter.java'), out_file)