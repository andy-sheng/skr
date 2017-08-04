#!/usr/bin/env python
#coding: UTF-8

import sys

class CommandInfo(object):
    def __init__(self):
        self.module = None
        self.source = None
        self.language = None
        self.package = None
        self.name = None

    def __str__(self):
        return ('module=%s source=%s language=%s name=%s'
                %(self.module, self.source, self.language, self.name))

def _parse_arg(index, argv, count):
    index += 1
    if index < count and not argv[index].startswith('-'):
        return argv[index]
    else:
        return None

def parse_command_info(argv):
    """Parse command info
    Command:
    parse_command_info [-m module] [-s source] [-l language] [-n name]
    Usage:
    -m module    The module name to add component in, when runs under project root path, this option
                 specifies which module to be used. If not specified, default value 'app' will be used.
                 When runs under a module path, module name can be inferred automatically.
    -s source    The source path to add component in. If not specified, default value 'src/main'
                 will be used.
    -l language  The Language type to be used, If not specified, default value 'java' will be used.
    -p package   The root package of the component to be created. If not specified, the root package
                 should be inferred from project structure.
    -n name      The name of the component to be created.
    -h           Show help message.
    """
    count = len(argv)
    if count <= 1:
        print "error: command argument is required!\n"
        print parse_command_info.__doc__
        return None
    i = 1
    command_info = CommandInfo()
    while i < count:
        option = argv[i]
        if option == '-h':
            print parse_command_info.__doc__
            i += 1
            return None
        arg = _parse_arg(i, argv, count)
        if option == '-m':
            command_info.module = arg
        elif option == '-s':
            command_info.source = arg
        elif option == '-l':
            command_info.language = arg
        elif option == '-p':
            command_info.package = arg
        elif option == '-n':
            command_info.name = arg
        else:
            print "error: unknown option type " + option
            return None
        if not arg:
            print "error: no arg found for option " + option
            return None
        else:
            i += 2
    return command_info