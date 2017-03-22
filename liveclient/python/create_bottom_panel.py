#!/usr/bin/env python
#coding: UTF-8

from template_base import *

# 包名
root_package = "com.wali.live.livesdk.live.liveshow";
# 文件目录
root_path = "../src/main/java/" + root_package.replace(".", "/")

# 生成Panel
class_package = root_package + ".view.panel"
class_import = "import " + root_package + ".presenter.panel." + name + "Presenter;"
view = TemplateInfo(class_package, class_import)
out_file = root_path + "/view/panel/" + name + "Panel.java"
checkPath(root_path + "/view/panel/")
view.writeToFile("./template/TemplatePanelView.java", out_file)

# 生成Presenter
class_package = root_package + ".presenter.panel"
class_import = "import " + root_package + ".view.panel." + name + "Panel;"
view = TemplateInfo(class_package, class_import)
out_file = root_path + "/presenter/panel/" + name + "Presenter.java"
checkPath(root_path + "/presenter/panel")
view.writeToFile("./template/TemplatePanelPresenter.java", out_file)