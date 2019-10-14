package com.module.playways.race.room.view.matchview

import android.animation.ValueAnimator
import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.U
import com.module.playways.R


// 单个滚动的匹配
// 循环滚动 两种方式实现，position无限大 和 在首尾加上一个view 采用无限大的方式
// 用recyler方式实现
class RaceMatchItemView : ConstraintLayout {

    private val totalTime: Long = 10 * 1000L  // 动画的总时间
    // todo 调太慢会导致减速转动的时间不够
    private val fastSpeed: Float = 1f         // 调整转动速度 通过一个pixel需要的时间(fastSpeed / U.getDisplayUtils().density)
    private val slowSpeed: Float = 1000f      // 最慢速度(slowSpeed / U.getDisplayUtils().density)
    private val itemHeight = U.getDisplayUtils().dip2px(100f)
    private val itemTime = itemHeight * (fastSpeed / U.getDisplayUtils().density) // 滚动一个itemView的时间

    private var target = 0  // 最终停的item

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val recyclerView: RecyclerView
    val scrollLinearLayoutManager: ScrollLinearLayoutManager
    val adapter: RaceMatchAdapter = RaceMatchAdapter()

    init {
        View.inflate(context, R.layout.race_match_view_item_layout, this)
        recyclerView = this.findViewById(R.id.recycler_view)
        scrollLinearLayoutManager = ScrollLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = scrollLinearLayoutManager
        recyclerView.adapter = adapter
    }

    fun setData(listData: ArrayList<UserInfoModel>?, uid: Int) {
        if (listData != null) {
            listData.forEachIndexed { index, model ->
                if (model.userId == uid) {
                    target = index
                    return@forEachIndexed
                }
            }
            adapter.mDataList.clear()
            adapter.mDataList = listData
            adapter.notifyDataSetChanged()
        } else {
            // todo 先用测试数据，可以在外面把数据设置进来（dataList 和 target）
            var dataList = ArrayList<UserInfoModel>()
            for (i in 0..5) {
                dataList.add(MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().myUserInfo))
            }
            target = 3

            adapter.mDataList.clear()
            adapter.mDataList = dataList
            adapter.notifyDataSetChanged()
        }

        if (!adapter.mDataList.isNullOrEmpty()) {
            // 第一次滚到指定位置的时间
            val diffTime = itemTime * target
            // 滚动一个周期的时间
            val oneCycleTime = itemTime * adapter.mDataList.size

            // 先快速滚动
            scrollLinearLayoutManager.setSpeedSlow(fastSpeed)
            recyclerView.smoothScrollToPosition(Int.MAX_VALUE)

            // 匀减速运动，留2个周期时间，滚一个周期，可调整（目前是2)
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
                recyclerView.smoothScrollToPosition(firstVisible + adapter.mDataList.size + target - firstVisible % adapter.mDataList.size)
            }, (totalTime - 2 * oneCycleTime - (totalTime - diffTime) % oneCycleTime).toLong())
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

}