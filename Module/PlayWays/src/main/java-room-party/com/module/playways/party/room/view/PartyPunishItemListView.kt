package com.module.playways.party.room.view

import android.animation.ValueAnimator
import android.content.Context
import android.os.Handler
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import com.common.utils.U
import com.module.playways.R
import com.module.playways.party.room.adapter.PartyPunishItemAdapter
import com.module.playways.party.room.model.PartyPunishInfoModel
import com.module.playways.race.room.view.matchview.ScrollLinearLayoutManager

// 单个滚动的匹配
// 循环滚动 两种方式实现，position无限大 和 在首尾加上一个view 采用无限大的方式
// 用recyler方式实现
class PartyPunishItemListView : ConstraintLayout {

    val totalTime: Long = 1500L  // 动画的总时间
    // todo 调太慢会导致减速转动的时间不够
    private val fastSpeed: Float = 1f         // 调整转动速度 通过一个pixel需要的时间(fastSpeed / U.getDisplayUtils().density)
    private val slowSpeed: Float = 500f      // 最慢速度(slowSpeed / U.getDisplayUtils().density)
    private val itemHeight = U.getDisplayUtils().dip2px(100f)
    val itemTime = itemHeight * (fastSpeed / U.getDisplayUtils().density) // 滚动一个itemView的时间

    private var target = 0  // 最终停的item

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val recyclerView: RecyclerView
    val scrollLinearLayoutManager: ScrollLinearLayoutManager
    val adapter = PartyPunishItemAdapter()
    val uiHandler = Handler()

    init {
        View.inflate(context, R.layout.race_match_view_item_layout, this)
        recyclerView = this.findViewById(R.id.recycler_view)
        scrollLinearLayoutManager = ScrollLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = scrollLinearLayoutManager
        recyclerView.adapter = adapter
    }

    fun setData(listData: ArrayList<PartyPunishInfoModel>?, index: Int, listener: () -> Unit) {
        if (listData != null) {
            var count = 0
            adapter.mDataList.clear()
            adapter.mDataList.addAll(listData)
            adapter.notifyDataSetChanged()
        }

        target = index

        if (!adapter.mDataList.isNullOrEmpty()) {
            uiHandler.post {
                // 第一次滚到指定位置的时间
                val diffTime = itemTime * target
                // 滚动一个周期的时间
                val oneCycleTime = itemTime * adapter.mDataList.size

                // 先快速滚动

                recyclerView.scrollToPosition(0)
                scrollLinearLayoutManager.setSpeedSlow(fastSpeed)
                recyclerView.smoothScrollToPosition(Int.MAX_VALUE)

                // 匀减速运动，留2个周期时间，滚一个周期，可调整（目前是2)
                var pauseTs = (totalTime - 5 * oneCycleTime - (totalTime - diffTime) % oneCycleTime).toLong()
                if (pauseTs < 0) {
                    pauseTs = 0
                }
                uiHandler.postDelayed(Runnable {
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
                }, pauseTs)

                uiHandler.postDelayed({
                    listener.invoke()
                }, totalTime)
            }
        } else {

        }
    }

    // 快速滚动的时间
    fun getFastTime(): Long {
        // 第一次滚到指定位置的时间
        val diffTime = itemTime * target
        // 滚动一个周期的时间
        val oneCycleTime = itemTime * adapter.mDataList.size
        return (totalTime - 5 * oneCycleTime - (totalTime - diffTime) % oneCycleTime).toLong()
    }

    // 滚动完成的时间
    fun getAnimationTime(): Long {
        // 滚动一个周期的时间
        val oneCycleTime = itemTime * adapter.mDataList.size
        return totalTime - 3 * oneCycleTime.toLong()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    fun reset() {
        recyclerView.stopScroll()
        uiHandler.removeCallbacksAndMessages(null)
    }
}