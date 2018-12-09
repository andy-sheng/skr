package com.module.rankingmode.prepare.view;

import java.util.List;

public interface ILayer {
    void addSprite(Sprite view);

    void removeSprite(Sprite view);

    Sprite getAllSprite();

    void addSpriteList(List<Sprite> viewList);

    void reset();
}
