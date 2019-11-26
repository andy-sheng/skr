package com.module.playways.room.room.comment.fly

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.common.anim.ObjectPlayControlTemplate
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.utils.dp
import com.component.level.utils.LevelConfigUtils
import com.module.playways.BaseRoomData
import com.module.playways.R
import com.module.playways.room.msg.event.CommentMsgEvent
import com.module.playways.room.room.comment.CommentView
import com.module.playways.room.room.comment.model.CommentModel
import com.module.playways.room.room.comment.model.CommentTextModel
import com.module.playways.room.room.event.PretendCommentMsgEvent
import kotlinx.coroutines.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.ArrayList
import kotlin.math.absoluteValue

// todo 飞行弹幕
class FlyCommentView(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs), CoroutineScope by MainScope() {

    private val TAG = "FlyCommentView"

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs)

    var roomData: BaseRoomData<*>? = null

    private val FLY_SPEED = 220  // 飞行速度 220px/s
    private val CACHE_NUMBER = 4 // 缓存数量
    private val ROAD_NUM = 4     // 道路数量
    private val BOTTOM_SPACING = 30 // view 底部流的空隙，防止最小面的那个空间太小，显示不全
    private val PLAYER_SPACING = 20 // 同一条跑道中两个选手的间距

    private val ITEM_HEIGHT = 26.dp()  // 每个飞行弹幕的高度

    private val mRoadEnterNumber = IntArray(ROAD_NUM) // 这条道路上有多少选手还处于进场状态，进场完成的标记是尾部进场
    private val mRoadRunNumber = IntArray(ROAD_NUM)   // 这条道路上有多少选手已经进场，处于奔跑状态

    private val mFlyCommentViewCache = ArrayList<FlyCommentViewWithExtraInfo>(4)  //缓存

    private val animatorSet = ArrayList<ObjectAnimator>()

    private var flyCommentControl: ObjectPlayControlTemplate<CommentModel, FlyCommentViewWithExtraInfo> = object : ObjectPlayControlTemplate<CommentModel, FlyCommentViewWithExtraInfo>() {
        override fun accept(cur: CommentModel): FlyCommentViewWithExtraInfo? {
            // 找到合适的路径来播放
            MyLog.d(TAG, "accept cur = $cur")
            return tryFindIdle(cur)
        }

        override fun onStart(model: CommentModel, consumer: FlyCommentViewWithExtraInfo) {
            // 飞弹幕
            MyLog.d(TAG, "onStart model = $model, consumer = $consumer")
            if (visibility == View.VISIBLE) {
                startPlayFly(model, consumer)
            }
        }

        override fun onEnd(model: CommentModel?) {

        }
    }

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    private fun startPlayFly(model: CommentModel, consumer: FlyCommentViewWithExtraInfo) {
        launch(Dispatchers.Main) {
            val textView = consumer.view
            val width = textView?.width ?: 0
            val time1 = (width + PLAYER_SPACING) * 1000 / FLY_SPEED

            val timeTotal = (getWidth() + width) * 1000 / FLY_SPEED

            val animator = ObjectAnimator.ofFloat(textView, View.TRANSLATION_X, getWidth().toFloat(), -width.toFloat())
            animator.duration = timeTotal.toLong()
            animator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {

                }

                override fun onAnimationEnd(animation: Animator?) {
                    removeItemView(consumer)
                    mRoadRunNumber[consumer.roadIndex]--
                    animatorSet.remove(animator)
                }

                override fun onAnimationCancel(animation: Animator?) {
                    removeItemView(consumer)
                    mRoadRunNumber[consumer.roadIndex]--
                    animatorSet.remove(animator)
                }

                override fun onAnimationStart(animation: Animator?) {
                    animatorSet.add(animator)

                }
            })
            animator.start()

            delay(time1.toLong())
            mRoadEnterNumber[consumer.roadIndex]--
            mRoadRunNumber[consumer.roadIndex]++
            flyCommentControl.endCurrent(null)
        }
    }


    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            reset()
            removeAllViews()
        }
    }

    fun reset() {
        if (!animatorSet.isNullOrEmpty()) {
            animatorSet.forEach {
                it.removeAllListeners()
                it.cancel()
            }
        }
        animatorSet.clear()
        for (i in 0 until ROAD_NUM) {
            mRoadEnterNumber[i] = 0
            mRoadRunNumber[i] = 0
        }
        flyCommentControl.reset()
    }

    fun destory() {
        cancel()
        reset()
        mFlyCommentViewCache.clear()
        flyCommentControl.destroy()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: CommentMsgEvent) {
        MyLog.d(TAG, "onEvent CommentMsgEvent = $event")
        if (visibility == View.VISIBLE) {
            val commentTextModel = CommentTextModel.parseFromEvent(event, roomData)
            flyCommentControl.add(commentTextModel, false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PretendCommentMsgEvent) {
        MyLog.d(TAG, "onEvent PretendCommentMsgEvent = $event")
        if (visibility == View.VISIBLE) {
            flyCommentControl.add(event.mCommentModel, false)
        }
    }

    fun tryFindIdle(model: CommentModel): FlyCommentViewWithExtraInfo? {
        // 先找是否有 空闲的道路 再找是否
        for (i in 0 until ROAD_NUM) {
            if (mRoadEnterNumber[i] < 0) {
                mRoadEnterNumber[i] = 0
            }
            if (mRoadRunNumber[i] < 0) {
                mRoadRunNumber[1] = 0
            }
            if (mRoadEnterNumber[i] == 0 && mRoadRunNumber[i] == 0) {
                // 有空闲的道路，可以直接进场
                val info = getFlyCommentView()
                mRoadEnterNumber[i]++
                info.roadIndex = i
                info.isWorking = true
                addViewToRoad(info)
                bindCommentData(model, info)
                return info
            }
        }

        //
        for (i in 0 until ROAD_NUM) {
            if (mRoadEnterNumber[i] == 0) {
                // 有道路空闲 可以进场
                val info = getFlyCommentView()
                mRoadEnterNumber[i]++
                info.roadIndex = i
                info.isWorking = true
                addViewToRoad(info)
                bindCommentData(model, info)
                return info
            }
        }
        return null
    }

    private fun bindCommentData(model: CommentModel, info: FlyCommentViewWithExtraInfo) {
        val spanUtils = SpanUtils().append("\u202D")
        if (model.userInfo != null
                && model.userInfo!!.ranking != null
                && LevelConfigUtils.getSmallImageResoucesLevel(model.userInfo!!.ranking.mainRanking) > 0) {
            val drawable = U.getDrawable(LevelConfigUtils.getSmallImageResoucesLevel(model.userInfo!!.ranking.mainRanking))
            drawable.setBounds(0, 0, U.getDisplayUtils().dip2px(22f), U.getDisplayUtils().dip2px(19f))
            spanUtils.appendImage(drawable, SpanUtils.ALIGN_CENTER)
        }
        if (!TextUtils.isEmpty(model.nameBuilder)) {
            spanUtils.append(model.nameBuilder!!)
        }

        if (model.userInfo!!.honorInfo != null && model.userInfo!!.honorInfo.isHonor()) {
            val honorDrawable = U.getDrawable(R.drawable.person_honor_icon)
            honorDrawable.setBounds(0, 0, U.getDisplayUtils().dip2px(23f), U.getDisplayUtils().dip2px(14f))
            spanUtils.appendImage(honorDrawable, SpanUtils.ALIGN_CENTER).append(" ")
        }

        if (!TextUtils.isEmpty(model.stringBuilder)) {
            spanUtils.append(model.stringBuilder!!)
        }
        spanUtils.append("\u202C")
        info.view?.text = spanUtils.create()
    }

    private fun addViewToRoad(info: FlyCommentViewWithExtraInfo) {
        var lp: LayoutParams? = info.view?.layoutParams as LayoutParams?
        if (lp == null) {
            lp = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        val parentHeight = this.height
        if (parentHeight >= ITEM_HEIGHT * ROAD_NUM) {
            lp.topMargin = parentHeight / ROAD_NUM * (ROAD_NUM - info.roadIndex - 1)
        } else {
            var temp = parentHeight - ITEM_HEIGHT
            if (temp < 0) {
                temp = 0
            }
            lp.topMargin = temp / (ROAD_NUM - 1) * (ROAD_NUM - info.roadIndex - 1)
        }
        this.addView(info.view, lp)
        info.view?.translationX = width.toFloat()
    }

    private fun removeItemView(info: FlyCommentViewWithExtraInfo) {
        removeView(info.view)
        info.isWorking = false
    }

    private fun getFlyCommentView(): FlyCommentViewWithExtraInfo {
        mFlyCommentViewCache.forEach {
            if (!it.isWorking) {
                return it
            }
        }

        val info = FlyCommentViewWithExtraInfo()
        info.view = TextView(context)
        info.view?.setPadding(10.dp(), 0, 10.dp(), 0)
        info.view?.background = U.getDrawable(R.drawable.fly_comment_view_bg)
        if (mFlyCommentViewCache.size < CACHE_NUMBER) {
            mFlyCommentViewCache.add(info)
        }
        return info
    }


    inner class FlyCommentViewWithExtraInfo {
        var view: TextView? = null// view实体
        var roadIndex: Int = 0// 道路索引
        var isWorking = false// 是否正在被使用
    }
}
