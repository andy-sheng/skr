#!/usr/bin/env python
#coding: UTF-8

import os
import shutil

def rename_engine(path):
    removed = False
    for file_item in os.listdir(path):
        (name, ext) = os.path.splitext(file_item)
        if name.endswith('.so') or name.endswith('.jar'):
            src_file = os.path.join(path, file_item)
            dst_file = os.path.join(path, name)
            shutil.move(src_file, dst_file)
            removed = True
            print "rename " + file_item + " to " + name
    return removed

def check_file_size(src_file, dst_file):
    src_name = os.path.basename(src_file)
    dst_name = os.path.basename(dst_file)
    src_size = os.path.getsize(src_file)
    if os.path.exists(dst_file):
        dst_size = os.path.getsize(dst_file)
        change = src_size - dst_size
        if change >= 1024 * 1024: # bigger than 1M
            print "CATION【size expand】new %s is %db bigger than old %s one, this is extremely bad!!!" % (src_name, change, dst_name)
        elif change >= 1024: # bigger than 1K
            print "INFO【size expand】new %s is %db bigger than old %s one, this is bad!!!" % (src_name, change, dst_name)
        elif change <= -1024: # smaller than 1K
            print "INFO【size shrink】new %s is %db smaller than old %s one, this is good!!!" % (src_name, -change, dst_name)
    else:
        change = src_size
        if change >= 1024 * 1024: # bigger than 1M
            print "CATION【size expand】new %s with %db is added as %s, this is extremely bad!!!" % (src_name, change, dst_name)
        elif change >= 1024: # bigger than 1K
            print "CATION【size expand】new %s with %db is added as %s, this is bad!!!" % (src_name, change, dst_name)

def dispatch_engine(path, target_path, target_items):
    for file_item in target_items:
        src_file = os.path.join(path, file_item)
        if not os.path.exists(src_file):
            print "CATION: missing " + src_file
            continue
        check_file_size(src_file, os.path.join(target_path, file_item))
        shutil.copy(src_file, target_path)
        print "copy " + file_item + " to " + target_path

def delete_engine(path):
    for file_item in os.listdir(path):
        src_file = os.path.join(path, file_item)
        os.remove(src_file)
        print "delete " + file_item

def rename_file(path, name_dict):
    removed = False
    for src_name in name_dict:
        dst_name = name_dict[src_name]
        src_file = os.path.join(path, src_name)
        dst_file = os.path.join(path, dst_name)
        check_file_size(src_file, dst_file)
        shutil.move(src_file, dst_file)
        removed |= True
        print "rename " + src_name + " to " + dst_name
    return removed

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

    ## copy file to module enginebase
    module_path = 'enginebase'
    print "\ndispatch files for " + module_path
    dispatch_engine(engine_path, os.path.join(project_path, module_path, jar_path),
                    ['devicemanager.jar', 'player.jar', 'webrtc.jar', 'xplatform_util.jar'])
    dispatch_engine(engine_path, os.path.join(project_path, module_path, so_path),
                    ['libgnustl_shared.so'])

    ## copy file to module enginelive
    module_path = 'enginelive'
    print "\ndispatch files for " + module_path
    dispatch_engine(engine_path, os.path.join(project_path, module_path, jar_path),
                    ['broadcaster.jar'])
    dispatch_engine(engine_path, os.path.join(project_path, module_path, so_path),
                    ['libbroadcast.so'])

    ## copy file to module pluginplayer
    module_path = 'pluginplayer'
    print "\ndispatch files for " + module_path
    dispatch_engine(engine_path, os.path.join(project_path, module_path, jar_path),
                    ['devicemanager.jar', 'player.jar', 'webrtc.jar', 'xplatform_util.jar'])
    dispatch_engine(engine_path, os.path.join(project_path, module_path, so_path),
                    ['libplayer_only.so', 'libgnustl_shared.so'])
    rename_file(os.path.join(project_path, module_path, so_path), {'libplayer_only.so':'libbroadcast.so'})

    ## copy file to module pluginplayerdemo
    # 拷贝与pluginplayer中相同的so即可
    module_path = 'pluginplayerdemo'
    print "\ndispatch files for " + module_path
    dispatch_engine(engine_path, os.path.join(project_path, module_path, so_path),
                    ['libplayer_only.so', 'libgnustl_shared.so'])
    rename_file(os.path.join(project_path, module_path, so_path), {'libplayer_only.so':'libbroadcast.so'})

    ret = raw_input("\ndispatch galileo files done,\ndo you want to delete all files under ./galileo path? y/n: ")
    if ret.lower() == 'y':
        delete_engine(engine_path)

    print "\ndispatch galileo files done, enjoy!!!"