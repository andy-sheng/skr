package com.module.playways.room.gift.model

class NormalGift : BaseGift() {

    override fun parseFromJson(extra: String) {

    }

    companion object {
        fun getFlower(): NormalGift {
            val baseGift: NormalGift = NormalGift()
            baseGift.balance = 0
            baseGift.isCanContinue = true
            baseGift.description = "鲜花"
            baseGift.displayType = 4
            baseGift.giftID = 1
            baseGift.giftName = "鲜花"
            baseGift.giftType = 1
            baseGift.giftURL = "http://res-static.inframe.mobi/gift/img/liwu_meigui.png"
            baseGift.isPlay = false
            baseGift.price = 0
            baseGift.realPrice = 0.0f
            baseGift.sortID = 1
            baseGift.sourceURL = ""
            baseGift.textContinueCount = 0
            return baseGift
        }
    }
}
