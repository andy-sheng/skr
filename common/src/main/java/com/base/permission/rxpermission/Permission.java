package com.base.permission.rxpermission;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


public class Permission {
    public final String name;
    public final boolean granted;
    public final boolean shouldShowRequestPermissionRationale;

    Permission(String name, boolean granted) {
        this(name, granted, false);
    }

    Permission(String name, boolean granted, boolean shouldShowRequestPermissionRationale) {
        this.name = name;
        this.granted = granted;
        this.shouldShowRequestPermissionRationale = shouldShowRequestPermissionRationale;
    }

    public boolean equals(Object o) {
        if(this == o) {
            return true;
        } else if(o != null && this.getClass() == o.getClass()) {
            Permission that = (Permission)o;
            return this.granted != that.granted?false:(this.shouldShowRequestPermissionRationale != that.shouldShowRequestPermissionRationale?false:this.name.equals(that.name));
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + (this.granted?1:0);
        result = 31 * result + (this.shouldShowRequestPermissionRationale?1:0);
        return result;
    }

    public String toString() {
        return "Permission{name=\'" + this.name + '\'' + ", granted=" + this.granted + ", shouldShowRequestPermissionRationale=" + this.shouldShowRequestPermissionRationale + '}';
    }
}
