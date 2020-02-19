package com.module.playways.grab.room.invite

import android.content.Intent
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.grab.room.invite.fragment.InviteFriendFragment2

@Route(path = RouterConstants.ACTIVITY_INVITE_FRIEND)
class InviteFriendActivity : BaseActivity() {
    companion object {
        var iInviteCallBack: IInviteCallBack? = null

        fun open(iInviteCallBack: IInviteCallBack) {
            this.iInviteCallBack = iInviteCallBack
            val intent = Intent(U.app(), InviteFriendActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            U.app().startActivity(intent)
        }
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        // 房主想要邀请别人加入游戏
        // 打开邀请面板
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, InviteFriendFragment2::class.java)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .addDataBeforeAdd(0, iInviteCallBack)
                .build())

        iInviteCallBack = null
    }

    override fun useEventBus(): Boolean {
        return false
    }
}