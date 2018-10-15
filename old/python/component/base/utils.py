import os

def check_path(path):
    if not path or not path.strip() or os.path.exists(path):
        return
    os.makedirs(path)
    pass