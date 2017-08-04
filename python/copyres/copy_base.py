from copy_entity import *

def parse_list(index, argv, count):
    index += 1
    res_list = []
    while index < count:
        item = argv[index]
        if item[0] == "-":
            break
        res_list.append(item)
        index += 1
    return res_list

def process(argv, count, org_res_path, dst_res_path):
    """
    Usage:
    -s [string source] [string source] ... (note: if use -S, a new empty line will be inserted before each item)
    -p [plurals source] [plurals source] ... (note: if use -P, a new empty line will be inserted before each item)
    -a [string-array source] [string-array source] ... (note: if use -A, a new empty line will be inserted before each item)
    -i [drawable source] [drawable source] ...(note: if use -I, items in xml drawable will be checked recursively)
    -D [dimen source] [dimen source] ...
    -L [layout source] [layout source] ...
    """
    i = 1
    while i < count:
        option = argv[i]
        if option.lower() == "-h" or option.lower() == "--help":
            print process.__doc__
            i += 1
            return
        res_list = parse_list(i, argv, count)
        if not res_list or len(res_list) == 0:
            print "error: no arg found for option " + option
            return
        copier = None
        if option.lower() == '-s':
            copier = CopyValueRes(org_res_path, dst_res_path, option == '-S', 'strings', 'string')
        elif option.lower() == "-p":
            copier = CopyValueRes(org_res_path, dst_res_path, option == '-P', 'strings', 'plurals')
        elif option.lower() == "-a":
            copier = CopyValueRes(org_res_path, dst_res_path, option == '-A', 'arrays', 'string-array')
        elif option.lower() == "-i":
            copier = CopyDrawableRes(org_res_path, dst_res_path, option == '-I')
        elif option.lower() == "-l":
            copier = CopyLayoutRes(org_res_path, dst_res_path)
        else:
            print "error: unknown option type " + option
            return
        copier.do_copy(res_list)
        i += len(res_list) + 1