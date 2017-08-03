#!/usr/bin/env python
#coding: UTF-8

import os

MODE_ALL = 0
MODE_ANY = 1

_DEFAULT_MODULE_PATH = 'app'
_DEFAULT_SRC_PATH = 'src/main'
_DEFAULT_SUB_PATH = 'java'
_DEFAULT_PACKAGE_PATH = 'com/thornbirds/component'

class PackageInfo(object):
    def __init__(self, project_path, module_path, src_path, sub_path, package_path):
        self.project_path = project_path
        self.module_path = module_path
        self.src_path = src_path
        self.sub_path = sub_path
        self.package_path = package_path

    def __str__(self):
        return ('project_path=%s module_path=%s src_path=%s sub_path=%s package_path=%s'
                %(self.project_path, self.module_path, self.src_path, self.sub_path, self.package_path))

    def get_class_package(self):
        return self.package_path.replace('/', '.')

    def get_class_path(self):
        return os.path.join(self.module_path, self.src_path, self.sub_path, self.package_path.replace('.', '/'))

    def get_src_path(self):
        return os.path.join(self.module_path, self.src_path)

def find_project_module_path(path, properties, mode = MODE_ALL):
    if not path or not path.strip():
        print "waring: input path is empty."
        return path
    if not properties or len(properties) == 0:
        print "waring: input properties is empty, curr path will be returned."
        return path
    count = 0
    for property in properties:
        property_path = os.path.join(path, property)
        if os.path.exists(property_path):
            count += 1
    if (mode == MODE_ANY and count >= 1) or (mode == MODE_ALL and count == len(properties)):
        return path
    parent_path = os.path.dirname(path)
    if not parent_path or parent_path == '/':
        return None
    else:
        return find_project_module_path(parent_path, properties, mode)

def find_package_info_from_path(path, module_path):
    remain_path = path[len(module_path):].strip('/ ')
    if not remain_path:
        return None, None, None
    src_path = sub_path = package_path = None
    split_path = remain_path.split('/')
    cnt = len(split_path)
    if cnt == 1:
        src_path = split_path[0] + '/main'
    elif cnt > 1:
        src_path = '/'.join(split_path[:2])
    if cnt > 2:
        sub_path = split_path[2]
    if cnt > 3:
        package_path = '/'.join(split_path[3:])
    return src_path, sub_path, package_path

def find_package_path_from_project(module_path, src_path):
    package = None
    manifest_path = os.path.join(module_path, src_path, 'AndroidManifest.xml')
    if os.path.exists(manifest_path):
        from lxml import etree
        xml_file = etree.parse(manifest_path)
        root_node = xml_file.getroot()
        package = root_node.attrib['package']
    if not package:
        split_path = module_path.strip('/ ').split('/')
        print "split_path is " + str(split_path)
        if split_path[-1] == 'app':
            print "waring: cannot infer package_path from %s. use 'com.@project'" %(manifest_path)
            return 'com.%s.component' %split_path[-2]
        else:
            print "waring: cannot infer package_path from %s. use 'com.@project.@module'" %(manifest_path)
            return 'com.%s.%s.component' %(split_path[-1], split_path[-2])
    else:
        return package + '.component'
    return None

def parse_package_info(path, module_type, src_type, sub_type, package_type):
    if not path or not path.strip():
        print "waring: input path is empty."
        return None

    # 获取模块路径
    module_path = find_project_module_path(path, ['build.gradle'], MODE_ALL)
    if not module_path or not module_path.strip():
        print "error: module path not found."
        return None

    # 获取工程路径
    project_path = find_project_module_path(module_path, ['build.gradle', 'settings.gradle'], MODE_ALL)
    if not project_path or not project_path.strip():
        print "error: project path not found."
        return None

    src_path, sub_path, package_path = find_package_info_from_path(path, module_path)
    # 推导src_path
    if not src_type:
        src_type = src_path if src_path else _DEFAULT_SRC_PATH
    elif src_path and src_type != src_path:
        result = raw_input("inferred src_path '%s' is different with specified one '%s', done you want to "
                       "use inferred one? y/n " %(src_path, src_type))
        if result == 'y':
            src_type = src_path
    # 推导sub_path
    if not sub_type:
        sub_type = sub_path if sub_path else _DEFAULT_SUB_PATH
    elif sub_path and sub_type != sub_path:
        result = raw_input("inferred sub_path '%s' is different with specified one '%s', done you want to "
                       "use inferred one? y/n " %(sub_path, sub_type))
        if result == 'y':
            sub_type = sub_path
    # 推导package_path
    if not package_type:
        package_type = package_path

    if module_path == project_path:
        if not module_type:
            module_type = _DEFAULT_MODULE_PATH
            # 若模块目录与工程目录相同，则说明是在工程目录运行的命令，则使用默认的模块(如app)
            print "waring: runs in project path, but module not specified, use default module '%s' instead." %module_type
        module_path = os.path.join(project_path, module_type)
    elif module_type:
        print "waring: runs in module path, specified module_type '%s' will be ignore." %module_type

    # 推导package_path
    if not package_type:
        package_path = find_package_path_from_project(module_path, src_type)
        package_type = package_path if package_path else _DEFAULT_PACKAGE_PATH

    return PackageInfo(project_path, module_path, src_type, sub_type, package_type)