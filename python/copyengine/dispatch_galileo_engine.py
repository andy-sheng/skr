#!/usr/bin/env python
#coding: UTF-8

import os
import shutil

def rename_engine(path):
    removed = False
    for fileItem in os.listdir(path):
        (name, ext) = os.path.splitext(fileItem)
        if name.endswith('.so') or name.endswith('.jar'):
            src_file = os.path.join(path, fileItem)
            dst_file = os.path.join(path, name)
            shutil.move(src_file, dst_file)
            removed = True
            print "rename " + fileItem + " to " + name
    return removed

def dispatch_engine(path, target_path, target_items):
    for fileItem in target_items:
        src_file = os.path.join(path, fileItem)
        if not os.path.exists(src_file):
            print "warning: missing " + src_file
            continue
        src_size = os.path.getsize(src_file)
        dst_file = os.path.join(target_path, fileItem)
        if os.path.exists(dst_file):
            dst_size = os.path.getsize(dst_file)
            change = src_size - dst_size
            if change >= 1024 * 1024: # bigger than 1M
                print "【####### size expand waring #######】new " + fileItem + " is " + str(change) + "b bigger than old one, this is extremely bad!!!"
            elif change >= 1024: # bigger than 1K
                print "【####### size expand info #######】new " + fileItem + " is " + str(change) + "b bigger than old one, this is bad!!!"
            elif change <= -1024: # smaller than 1K
                print "【####### size shrink info #######】new " + fileItem + " is " + str(-change) + "b smaller than old one, this is good!!!"
        else:
            change = src_size
            if change >= 1024 * 1024: # bigger than 1M
                print "【####### size expand waring #######】new " + fileItem + " with " + str(change) + "b is added, this is extremely bad!!!"
            elif change >= 1024: # bigger than 1K
                print "【####### size expand info #######】new " + fileItem + " with " + str(change) + "b is added, this is bad!!!"
        shutil.copy(src_file, target_path)
        print "copy " + fileItem + " to " + target_path

def delete_engine(path):
    for fileItem in os.listdir(path):
        src_file = os.path.join(path, fileItem)
        os.remove(src_file)
        print "delete " + fileItem

if __name__ == '__main__':
    engine_path = os.path.abspath('./galileo')
    if not os.path.exists(engine_path):
        os.makedirs(engine_path)

    ret = rename_engine(engine_path)
    if not ret:
        code = raw_input("engine not updated, since no '.jar.*' or '.so.*' files found under path './galileo',\n" +
                    "do you want to continue any more? y/n: ")
        if code.lower() != 'y':
            exit()

    project_path = os.path.abspath('../..')
    jar_path = 'libs'
    so_path = 'src/main/jniLibs/armeabi-v7a'

    # copy file to module enginebase
    module_path = 'enginebase'
    print "\ndispatch files for " + module_path
    dispatch_engine(engine_path, os.path.join(project_path, module_path, jar_path),
                    ['devicemanager.jar', 'player.jar', 'webrtc.jar', 'xplatform_util.jar'])
    dispatch_engine(engine_path, os.path.join(project_path, module_path, so_path),
                    ['libgnustl_shared.so'])

    # copy file to module enginelive
    module_path = 'enginelive'
    print "\ndispatch files for " + module_path
    dispatch_engine(engine_path, os.path.join(project_path, module_path, jar_path),
                    ['broadcaster.jar'])
    dispatch_engine(engine_path, os.path.join(project_path, module_path, so_path),
                    ['libbroadcast.so'])

    ret = raw_input("\ndispatch galileo files done,\ndo you want to delete all files under ./galileo path? y/n: ")
    if ret.lower() == 'y':
        delete_engine(engine_path)

    print "\ndispatch galileo files done, enjoy!!!"