#!/usr/bin/env python
#coding: UTF-8

from template_base import *

# 包名
root_package = "com.wali.live.livesdk.live.component.data";
# 文件目录
root_path = "../src/main/java/" + root_package.replace(".", "/")

# 生成Presenter
class_package = root_package
class_import = ""
view = TemplateInfo(class_package, class_import)
out_file = root_path + "/" + name + "Presenter.java"
checkPath(root_path)
view.writeToFile("./template/TemplateDataPresenter.java", out_file)