package com.module.home.game.view

import com.component.busilib.friends.RecommendModel
import com.component.busilib.friends.SpecialModel
import com.module.home.model.GameKConfigModel
import com.module.home.model.SlideShowModel

interface IQuickGameView3 {
    fun setBannerImage(slideShowModelList: List<SlideShowModel>?)

    fun setRecommendInfo(list: MutableList<RecommendModel>?)

    //    void setGameConfig(GameKConfigModel gameKConfigModel);

    fun showTaskRedDot(show: Boolean)

    fun showRemainTimes(remainTimes : Int)
}
