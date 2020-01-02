package com.module.playways.party.home

import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.playways.R

@Route(path = RouterConstants.ACTIVITY_PARTY_HOME)
class PartyHomeActivity : BaseActivity() {

    lateinit var title: CommonTitleBar
    lateinit var backIv: ImageView
    lateinit var createRoom: ImageView
    lateinit var contentArea: RelativeLayout

    var partyRoomView: PartyRoomView? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.party_home_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

        title = findViewById(R.id.title)
        backIv = findViewById(R.id.back_iv)
        createRoom = findViewById(R.id.create_room)
        contentArea = findViewById(R.id.content_area)


    }

    override fun useEventBus(): Boolean {
        return false
    }
}