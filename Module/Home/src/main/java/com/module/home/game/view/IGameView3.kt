package com.module.home.game.view

import com.module.home.model.GameKConfigModel

interface IGameView3 {
    fun setGameConfig(gameKConfigModel: GameKConfigModel)

    //显示红包运营为
    fun showRedOperationView(homepagesitefirstBean: GameKConfigModel.HomepagesitefirstBean)

    fun hideRedOperationView()
}
