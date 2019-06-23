package com.module.home.game.view

import com.component.busilib.friends.SpecialModel
import com.module.home.model.GameKConfigModel
import com.module.home.model.SlideShowModel

interface IQuickGameView3 {
    fun setBannerImage(slideShowModelList: List<SlideShowModel>)

    //    void setRecommendInfo(List<RecommendModel> list);

    fun setQuickRoom(list: MutableList<SpecialModel>, offset: Int)

    //    void setGameConfig(GameKConfigModel gameKConfigModel);

    fun showTaskRedDot(show: Boolean)
}
