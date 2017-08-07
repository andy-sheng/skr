import os

class TemplateInfo:
    def __init__(self, package, imports, name1, name2, command, user_name, user_date):
        self.package = package
        self.imports = imports
        self.name1 = name1
        self.name2 = name2
        self.command = command
        self.user = user_name
        self.date = user_date

    def write_to_file(self, tmplfile, outfile):
        if os.path.exists(outfile):
            print "warning: " + outfile + " already existed!"
            return
        content = ""
        input = open(tmplfile)
        try:
            content = input.read()
        finally:
            input.close()

        content = (content.replace("${PACKAGE}", self.package)
                   .replace("${IMPORT}", self.imports)
                   .replace("${USER}", self.user)
                   .replace("${DATE}", self.date)
                   .replace("${NAME1}", self.name1)
                   .replace("${NAME2}", self.name2)
                   .replace("${COMMAND}", self.command))

        out = open(outfile, "w")
        out.write(content)
        out.close()

        print "info: generate " + outfile + " done!"