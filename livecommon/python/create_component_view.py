#!/usr/bin/env python
#coding: UTF-8

from template_base import *

# 包名
root_package = "com.wali.live.component";
# 文件目录
root_path = "../src/main/java/" + root_package.replace(".", "/")

# 生成View
class_package = root_package + ".view"
class_import = "import " + root_package + ".presenter." + name + "Presenter;"
view = TemplateInfo(class_package, class_import)
out_file = root_path + "/view/" + name + "View.java"
checkPath(root_path + "/view/")
view.writeToFile("./template/TemplateView.java", out_file)

# 生成Presenter
class_package = root_package + ".presenter"
class_import = "import " + root_package + ".view." + name + "View;"
view = TemplateInfo(class_package, class_import)
out_file = root_path + "/presenter/" + name + "Presenter.java"
checkPath(root_path + "/presenter/")
view.writeToFile("./template/TemplatePresenter.java", out_file)
