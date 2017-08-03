#!/usr/bin/env python
# coding: utf-8

import os
import shutil

class CopyFileRes(object):
    def __init__(self, org_res_path, dst_res_path):
        self.org_res_path = org_res_path
        self.dst_res_path = dst_res_path

    def _copy_item(self, item):
        pass

    def do_copy(self, item_list):
        if not item_list or len(item_list) == 0:
            print "info: do_copy but item_list is empty"
            return
        print "info: do_copy item_list: " + ", ".join(item_list)
        for src_item in item_list:
            self._copy_item(src_item)

# layout资源拷贝
class CopyLayoutRes(CopyFileRes):
    def __init__(self, org_res_path, dst_res_path):
        CopyFileRes.__init__(self, org_res_path, dst_res_path)

    def _copy_item(self, item):
        ret = self.__do_copy('layout', item)
        if ret:
            print "info: copy layout %s done" %item
        else:
            print "warning: copy layout %s failed" %item

    def __do_copy(self, res_path, filename):
        src_file = os.path.join(self.org_res_path, res_path, filename + '.xml')
        dst_path = os.path.join(self.dst_res_path, res_path)
        print 'info: src_file=%s dist_file=%s' %(src_file, dst_path)

        if not os.path.exists(dst_path):
            os.makedirs(dst_path)

        try:
            shutil.copy(src_file, dst_path)
            return True
        # eg. src and dest are the same file
        except shutil.Error as e:
            print('Error: 1 %s' % e)
        # eg. source or destination doesn't exist
        except IOError as e:
            print('Error: 2 %s' % e.strerror)
        return False

# 图片资源拷贝
class CopyDrawableRes(CopyFileRes):
    def __init__(self, org_res_path, dst_res_path, ensure_item):
        CopyFileRes.__init__(self, org_res_path, dst_res_path)
        self.ensure_item = ensure_item

    def _copy_item(self, item):
        ret = self.__do_copy('drawable', item)
        if ret:
            print "info: copy drawable %s done" %item
            return
        ret = self.__do_copy('drawable-xxhdpi', item)
        if ret:
            print "info: copy drawable-xxhdpi %s done" %item
        else:
            print "warning: copy drawable %s failed" %item

    def __do_copy(self, res_path, res_item):
        src_path = os.path.join(self.org_res_path, res_path)
        dst_path = os.path.join(self.dst_res_path, res_path)
        res_file = ""
        for filename in os.listdir(src_path):
            (name, ext) = os.path.splitext(filename)
            if name == res_item or name == res_item + '.9':
                res_file = filename
                break

        if not res_file or not res_file.strip():
            print "warning: res not found in src path"
            return False

        src_file = os.path.join(src_path, res_file)
        dst_file = os.path.join(dst_path, res_file)
        if not os.path.isfile(src_file):
            print "warning: cannot copy path, can only copy file"
            return False
        if os.path.exists(dst_file):
            print "warning: res already exists in dst path"
            if self.ensure_item and res_file.lower().endswith('.xml'):
                self.__ensure_item(dst_file)
                return True
            return False

        shutil.copy(src_file, dst_file)

        if self.ensure_item and res_file.lower().endswith(".xml"):
            self.__ensure_item(dst_file)

        return True

    def __ensure_item(self, xml_file):
        print "info: ensure item for: " + xml_file
        content = ""
        input = open(xml_file, "r")
        try:
            content = input.read()
        finally:
            input.close()

        sub_item_list = []
        start = 0
        while True:
            start = content.find('"@drawable/', start)
            if start == -1:
                break
            end = content.find('"', start + 1)
            if end == -1:
                break
            sub_item_list.append(content[(start + len('"@drawable/')):end])
            start = end

        if sub_item_list and len(sub_item_list) > 0:
            self.do_copy(sub_item_list)

class CopyItemRes(object):
    def __init__(self, org_res_path, dst_res_path):
        self.org_res_path = org_res_path
        self.dst_res_path = dst_res_path

# 字符串资源拷贝
class CopyValueRes(CopyItemRes):
    add_extra_line = False  # 是否字符串前增加空行
    sub_path = 'strings'  # 资源路径，默认为strings
    sub_type = 'string'  # 资源类别，默认为string

    def __init__(self, org_res_path, dst_res_path, add_extra_line, sub_path, sub_type):
        CopyItemRes.__init__(self, org_res_path, dst_res_path)
        self.add_extra_line = add_extra_line
        if sub_path and sub_path.strip():
            self.sub_path = sub_path
        if sub_type and sub_type.strip():
            self.sub_type = sub_type

    def do_copy(self, item_list):
        print "info: copy value resource in %s of %s for: %s" %(self.sub_path, self.sub_type, ", ".join(item_list))
        if not item_list or len(item_list) == 0:
            return
        print "info: copy default"
        self.__do_copy(os.path.join('values', self.sub_path + '.xml'), item_list)
        print "info: copy zh-rCN"
        self.__do_copy(os.path.join('values-zh-rCN', self.sub_path + '.xml'), item_list)
        print "info: copy zh-rTW"
        self.__do_copy(os.path.join('values-zh-rTW', self.sub_path + '.xml'), item_list)
        pass

    def __do_copy(self, res_path, res_list):
        src_file = os.path.join(self.org_res_path, res_path)
        dst_file = os.path.join(self.dst_res_path, res_path)

        if not os.path.exists(src_file):
            print "waring: src_file %s not exists" %src_file
            return

        if not os.path.exists(dst_file):
            print "waring: dst_file %s not exists" %dst_file
            return

        # 读取原始数据
        src_data = ""
        input = open(src_file)
        try:
            src_data = input.read()
        finally:
            input.close()

        # 读取目标数据
        out = open(dst_file, "r")
        try:
            dst_data = out.read()
        finally:
            out.close()

        out = open(dst_file, "w")

        # 定位在文件末尾的写入位置
        dst_pos = dst_data.rfind('</resources>')
        if dst_pos == -1:
            print "warning: cannot find </resources>"
            return

        # 依次处理每个待处理的项
        for item in res_list:
            header = '<' + self.sub_type + ' name="' + item + '"'
            tailor = '</' + self.sub_type + '>'
            pos = dst_data.find(header)
            if pos != -1:
                # 目标中已有该项，则跳过不处理
                print "warning: item " + item + " already exists in dst"
                continue
            start = src_data.find(header)
            if start == -1:
                print "warning: cannot find item " + item + " in src"
                continue
            end = src_data.find(tailor, start)
            content = '    ' + src_data[start:(end + len(tailor))] + '\n'
            if self.add_extra_line:  # 增加空行
                content = '\n' + content
                pass
            dst_data = dst_data[:dst_pos] + content + dst_data[dst_pos:]
            dst_pos += len(content)
            pass

        # 写入新的目标数据
        try:
            out.write(dst_data)
        finally:
            out.close()

