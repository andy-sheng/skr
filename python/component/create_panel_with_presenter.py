#!/usr/bin/env python
#coding: UTF-8

import os

from base.creator_base import *
from base.utils import *
from base.template_info import *

# 生成Panel
class_package = root_package + '.view.panel'
class_import = 'import %s.presenter.panel.%sPresenter;' %(root_package, name)
template_view = TemplateInfo(class_package, class_import, name, command, user_name, user_date)
out_file = os.path.join(root_path, 'view', 'panel', name + 'Panel.java')
check_path(os.path.join(root_path, 'view', 'panel'))
template_view.write_to_file(os.path.join(template_path, 'view', 'panel', 'PanelView.java'), out_file)

# 生成Presenter
class_package = root_package + '.presenter.panel'
class_import = 'import %s.view.panel.%sPanel;' %(root_package, name)
template_presenter = TemplateInfo(class_package, class_import, name, command, user_name, user_date)
out_file = os.path.join(root_path, 'presenter', 'panel', name + 'Presenter.java')
check_path(os.path.join(root_path, 'presenter', 'panel'))
template_presenter.write_to_file(os.path.join(template_path, 'presenter', 'panel', 'PanelPresenter.java'), out_file)