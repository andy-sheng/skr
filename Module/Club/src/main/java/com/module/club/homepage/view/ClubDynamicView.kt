package com.module.club.homepage.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.log.MyLog
import com.common.utils.U
import com.module.ModuleServiceManager
import com.module.club.R
import com.module.common.IBooleanCallback
import com.module.post.IDynamicPostsView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

// 家族动态
class ClubDynamicView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    var clubMemberInfo: ClubMemberInfo? = null

    private val dynamicView: IDynamicPostsView by lazy {
        val postModuleService = ModuleServiceManager.getInstance().postsService
        postModuleService.getDynamicPostsView((context as FragmentActivity), 6)
    }
    private var contentLinearLayout: LinearLayout? = null


    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    init {
        val height: Int = U.getDisplayUtils().screenHeight - U.getDisplayUtils().dip2px(243f);
        View.inflate(context, R.layout.club_tab_dynamic_view_layout, this)
        contentLinearLayout = this.findViewById(R.id.ll_content)
        val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height)

        val childView = dynamicView as View
        if (contentLinearLayout?.indexOfChild(childView) == -1) {
            if (childView.parent != null) {
                (childView.parent as ViewGroup).removeView(childView)
            }
            contentLinearLayout?.addView(childView,params)
//            contentLinearLayout?.addView(childView)
        }

        MyLog.d("lijianqun $dynamicView = $dynamicView : ......height = $height")
    }

    fun loadData(flag: Boolean, callBack: IBooleanCallback) {
        dynamicView.loadData(clubMemberInfo?.club?.clubID!!, callBack)
    }

    fun loadMoreData( callBack: IBooleanCallback) {
        dynamicView.loadMoreData(callBack)
    }


    fun stopPlay(){

    }

    fun destroy() {
        stopPlay()
        cancel()
        dynamicView.cancel()
    }
}