package com.engine.agora.effect;

public class EffectModel {
    int id;
    String name;
    String path;
    int drawableIdForIcon;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getDrawableIdForIcon() {
        return drawableIdForIcon;
    }

    public void setDrawableIdForIcon(int drawableIdForIcon) {
        this.drawableIdForIcon = drawableIdForIcon;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof EffectModel){
            EffectModel other = (EffectModel) obj;
            return other.getPath().equals(path);
        }
        return super.equals(obj);
    }
}
