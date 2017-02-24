#!/usr/bin/env python
#coding: UTF-8

import sys
import os
import time
import getpass

# 获取用户名及日期
user_name = getpass.getuser()
user_date = time.strftime('%Y/%m/%d',time.localtime(time.time()))

class TemplateInfo:
	PACKAGE = ""
	IMPORT = ""
	USER = ""
	DATE = ""
	NAME = ""
	
	def __init__(self, PACKAGE, IMPORT, NAME):
		global user_name, user_date
		self.PACKAGE = PACKAGE
		self.IMPORT = IMPORT
		self.USER = user_name
		self.DATE = user_date
		self.NAME = NAME

	def __del__(self):
		pass

	def writeToFile(self, tmplFile, outFile):
		if os.path.exists(outFile):
        		print "warning: " + outFile + " already existed!"
        		return
		content = ""
		input = open(tmplFile);
		try:
			content = input.read();
		finally:
			input.close()
		
		content = content.replace("${PACKAGE}", self.PACKAGE) \
			.replace("${IMPORT}", self.IMPORT) \
			.replace("${USER}", self.USER) \
			.replace("${DATE}", self.DATE) \
			.replace("${NAME}", self.NAME)
		
		out = open(outFile, "w")
		out.write(content);
		out.close();

        	print "generate " + outFile + " done!"

# 获取类名
if len(sys.argv) <= 1 or not sys.argv[1].strip():
    print "error: bad argument, a component name is required!"
    exit()
name = sys.argv[1]

# 包名
root_package = "com.wali.live.watchsdk.component";
# 文件目录
root_path = "../src/main/java/" + root_package.replace(".", "/")

# 生成View
class_package = root_package + ".view"
class_import = "import " + root_package + ".presenter." + name + "Presenter;"
view = TemplateInfo(class_package, class_import, name)
out_file = root_path + "/view/" + name + "View.java"
view.writeToFile("./TemplateView.java", out_file)

# 生成Presenter
class_package = root_package + ".presenter"
class_import = "import " + root_package + ".view." + name + "View;"
view = TemplateInfo(class_package, class_import, name)
out_file = root_path + "/presenter/" + name + "Presenter.java"
view.writeToFile("./TemplatePresenter.java", out_file)
