#!/usr/bin/env python
#coding: UTF-8

import os

from base.creator_base import *
from base.template_info import *
from base.utils import *

# 生成Panel
name1 = name + 'Panel'
name2 = name + 'Presenter'
class_package = root_package + '.view.panel'
class_import = 'import %s.presenter.panel.%s;' %(root_package, name2)
template_view = TemplateInfo(class_package, class_import, name1, name2, command, user_name, user_date)
out_file = os.path.join(root_path, 'view', 'panel', name1 + '.java')
check_path(os.path.dirname(out_file))
template_view.write_to_file(os.path.join(template_path, 'view', 'panel', 'PanelView.java'), out_file)

# 生成Presenter
name1 = name + 'Presenter'
name2 = name + 'Panel'
class_package = root_package + '.presenter.panel'
class_import = 'import %s.view.panel.%s;' %(root_package, name2)
template_presenter = TemplateInfo(class_package, class_import, name1, name2, command, user_name, user_date)
out_file = os.path.join(root_path, 'presenter', 'panel', name1 + '.java')
check_path(os.path.dirname(out_file))
template_presenter.write_to_file(os.path.join(template_path, 'presenter', 'panel', 'PanelPresenter.java'), out_file)