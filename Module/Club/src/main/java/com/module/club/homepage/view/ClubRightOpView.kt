package com.module.club.homepage.view

import android.view.View
import android.view.ViewStub
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.notification.event.RongClubMsgEvent
import com.common.view.ExViewStub
import com.common.view.ex.ExTextView
import com.module.ModuleServiceManager
import com.module.club.R
import com.module.common.ICallback
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * 家族首页底部操作按钮
 */
class ClubRightOpView(viewStub: ViewStub): ExViewStub(viewStub), ICallback {

    private val TAG = "ClubRightOpView"
    private var mApplyTv:ExTextView? = null
    private var mConversationTv:ExTextView? = null
    private var mPostTv: ExTextView? = null
    private var mConversationUnreadNumTv:ExTextView? = null
    private var mClubMemberInfo:ClubMemberInfo? = null

    override fun init(parentView: View) {
        mApplyTv = parentView.findViewById(R.id.club_right_apply_tv)
        mConversationTv = parentView.findViewById(R.id.club_right_conversation_tv)
        mPostTv = parentView.findViewById(R.id.club_right_post_tv)
        mConversationUnreadNumTv = parentView.findViewById(R.id.club_conversation_unread_num_tv)

    }

    fun bindData(clubMemberInfo:ClubMemberInfo?){
        mClubMemberInfo = clubMemberInfo

        mApplyTv?.setDebounceViewClickListener{

        }

        mConversationTv?.setDebounceViewClickListener {view->
            view?:return@setDebounceViewClickListener
            val club = clubMemberInfo?.club?: MyLog.e(TAG, "未获取到家族信息").let {
                return@setDebounceViewClickListener
            }

            club.name.let {
                ModuleServiceManager.getInstance().msgService.startClubChat(view.context, club.clubID.toString(), it)
            }
        }

        mPostTv?.setDebounceViewClickListener { view->
            updateUnreadMsgCount()
        }



    }

    override fun layoutDesc(): Int {
        return R.layout.club_home_page_right_op_view_layout
    }

    fun show(){
        setVisibility(View.VISIBLE)
    }

    fun hide(){
        setVisibility(View.GONE)
    }

    fun updateUnreadMsgCount(){
        //获取未读消息数
        mClubMemberInfo?.club?.let {
            ModuleServiceManager.getInstance().msgService.getClubUnReadCount(this, it.clubID.toString())
        }
    }

    /**
     * 获取并显示未读消息数量
     */
    override fun onSucess(obj: Any?) {
        obj?.let { it as? Int }?.let {
            if(it > 0) {
                mConversationUnreadNumTv?.text = it.toString()
                mConversationUnreadNumTv?.visibility = View.VISIBLE
            }else{
                mConversationUnreadNumTv?.text = ""
                mConversationUnreadNumTv?.visibility = View.GONE
            }
        }
    }

    override fun onFailed(obj: Any?, errcode: Int, message: String?) {
        MyLog.e(TAG, "获取未读消息数失败 errode: " + obj.toString() )
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RongClubMsgEvent){
        updateUnreadMsgCount()
    }

    override fun onViewAttachedToWindow(v: View) {
        super.onViewAttachedToWindow(v)
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}