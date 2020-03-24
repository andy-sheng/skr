package com.component.notification

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View

import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.busilib.view.AvatarView
import com.component.busilib.view.NickNameView
import com.module.ModuleServiceManager

class RongMsgNotifyView : ConstraintLayout {

    internal var mAvatarIv: AvatarView? = null
    internal var mNameView: NickNameView? = null
    internal var mHintTv: ExTextView? = null
    internal var mAgreeButton: ExTextView? = null

    var mUserInfoModel: UserInfoModel? = null
    var conversationType:String? = null

    var listener: (() -> Unit)? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        View.inflate(context, R.layout.rong_msg_notify_view_layout, this)

        mAvatarIv = findViewById(R.id.avatar_iv)
        mNameView = findViewById(R.id.name_view)
        mHintTv = findViewById(R.id.hint_tv)
        mAgreeButton = findViewById(R.id.agree_button)

        mAgreeButton?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                go()
            }
        })
        this.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                go()
            }
        })
    }

    private fun go() {
        listener?.invoke()

        //判断是家族群聊或私信
        if(ModuleServiceManager.getInstance().msgService.isPrivateMsg(conversationType)) {
            val needPop = ModuleServiceManager.getInstance().msgService.startPrivateChat(U.getActivityUtils().topActivity,
                    mUserInfoModel?.userId.toString(),
                    mUserInfoModel?.nicknameRemark,
                    mUserInfoModel?.isFriend == true
            )
        }else{
            val needPop = ModuleServiceManager.getInstance().msgService.startClubChat(U.getActivityUtils().topActivity,
                    mUserInfoModel?.clubInfo?.club?.clubID?.toString(),
                    mUserInfoModel?.clubInfo?.club?.name
            )
        }
//            if (needPop) {
//                activity?.finish()
//            }
    }

    fun bindData(userInfoModel: UserInfoModel?, text: String?, conversationType: String?) {
        this.mUserInfoModel = userInfoModel
        val isPrivateMsg = ModuleServiceManager.getInstance().msgService.isPrivateMsg(conversationType);

        if(isPrivateMsg){
            mHintTv?.text = text
            mAvatarIv?.bindData(userInfoModel)
            mNameView?.setAllStateText(userInfoModel)
        }else{
            mHintTv?.text = SpanUtils()
                    .append(("${userInfoModel?.nickname?:""}: ")).setForegroundColor(Color.parseColor("#F5A623"))
                    .append(text?:"").setForegroundColor(Color.parseColor("#505050"))
                    .create()

            mAvatarIv?.bindData(userInfoModel, userInfoModel?.clubInfo?.club?.logo)
            mNameView?.setAllStateText(userInfoModel?.clubInfo?.club?.name, null, null)
        }

        this.conversationType = conversationType
    }

}
