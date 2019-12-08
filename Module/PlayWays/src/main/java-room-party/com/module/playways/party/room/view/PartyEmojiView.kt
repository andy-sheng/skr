package com.module.playways.party.room.view

import android.view.View
import android.view.ViewStub
import com.common.view.ExViewStub
import com.module.playways.R

class PartyEmojiView(viewStub: ViewStub) : ExViewStub(viewStub) {
    override fun init(parentView: View) {

    }

    override fun layoutDesc(): Int {
        return R.layout.party_emoji_view_layout
    }
}