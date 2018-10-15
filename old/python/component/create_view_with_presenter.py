#!/usr/bin/env python
#coding: UTF-8

import os

from base.creator_base import *
from base.utils import *
from base.template_info import *

# 生成View
name1 = name + 'View'
name2 = name + 'Presenter'
class_package = root_package + '.view'
class_import = 'import %s.presenter.%s;' %(root_package, name2)
template_view = TemplateInfo(class_package, class_import, name1, name2, command, user_name, user_date)
out_file = os.path.join(root_path, 'view', name1 + '.java')
check_path(os.path.dirname(out_file))
template_view.write_to_file(os.path.join(template_path, 'view', 'ComponentView.java'), out_file)

# 生成Presenter
name1 = name + 'Presenter'
name2 = name + 'View'
class_package = root_package + '.presenter'
class_import = 'import %s.view.%s;' %(root_package, name2)
template_presenter = TemplateInfo(class_package, class_import, name1, name2, command, user_name, user_date)
out_file = os.path.join(root_path, 'presenter', name1 + '.java')
check_path(os.path.dirname(out_file))
template_presenter.write_to_file(os.path.join(template_path, 'presenter', 'ComponentPresenter.java'), out_file)