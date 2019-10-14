package com.module.playways.room.room.matchview

import android.animation.ValueAnimator
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.utils.U
import com.common.utils.dp
import com.module.playways.R
import com.youth.banner.BannerScroller
import com.youth.banner.WeakHandler


// 单个滚动的匹配
// 循环滚动 两种方式实现，position无限大 和 在首尾加上一个view 采用无限大的方式
// 用recyler方式实现
// todo 再算算时间吧
class RankMatchItemView : ConstraintLayout {

    private val totalTime: Long = 10 * 1000L  // 动画的总时间
    private val fastSpeed: Float = 1f  //通过一个pixel需要的时间
    private val slowSpeed: Float = 1000f  // 最慢速度
    private val itemHeight = U.getDisplayUtils().dip2px(100f)

    private var target = 0  // 最终停的item

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val recyclerView: RecyclerView
    val scrollLinearLayoutManager: ScrollLinearLayoutManager
    val adapter: RankMatchAdapter = RankMatchAdapter()

    init {
        View.inflate(context, R.layout.rank_match_view_item_layout, this)
        recyclerView = this.findViewById(R.id.recycler_view)
        scrollLinearLayoutManager = ScrollLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = scrollLinearLayoutManager
        recyclerView.adapter = adapter
    }

    fun setData() {
        // 测试数据
        var dataList = ArrayList<UserInfoModel>()
        for (i in 0..5) {
            dataList.add(MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().myUserInfo))
        }
        target = 3

        adapter.mDataList.clear()
        adapter.mDataList = dataList
        adapter.notifyDataSetChanged()

        // 滚动一个itemView的时间
        val itemTime = itemHeight * (fastSpeed / U.getDisplayUtils().density)
        // 第一次滚到指定位置的时间
        val diffTime = itemTime * target
        // 滚动一个周期的时间
        val oneCycleTime = itemTime * adapter.mDataList.size

        MyLog.d("RankMatchItemView", "itemTime = $itemTime diffTime = $diffTime oneCycle = $oneCycleTime")
        // 先快速滚动
        scrollLinearLayoutManager.setSpeedSlow(fastSpeed)
        recyclerView.smoothScrollToPosition(Int.MAX_VALUE)

        MyLog.d("RankMatchItemView", "setData ${(totalTime - 2 * oneCycleTime - (totalTime - diffTime) % oneCycleTime)}")
        recyclerView.postDelayed(Runnable {
            // 当前滑动的位置
            val firstVisible = scrollLinearLayoutManager.findFirstVisibleItemPosition()
            val mAnimator = ValueAnimator.ofFloat(0f, 1f)
            mAnimator.duration = (2 * oneCycleTime).toLong()
            mAnimator.interpolator = LinearInterpolator()
            mAnimator.addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                scrollLinearLayoutManager.setSpeedSlow(fastSpeed + (slowSpeed - fastSpeed) * value)
            }
            recyclerView.smoothScrollToPosition(firstVisible + adapter.mDataList.size)
        }, (totalTime - 2 * oneCycleTime - (totalTime - diffTime) % oneCycleTime).toLong())
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

}