package com.module.playways.race.room.view.matchview

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.race.room.view.matchview.widget.OnWheelScrollListener
import com.module.playways.race.room.view.matchview.widget.WheelView
import com.module.playways.race.room.view.matchview.widget.adapters.AbstractWheelAdapter

// 单个滚动的匹配
// 循环滚动 两种方式实现，position无限大 和 在首尾加上一个view 采用无限大的方式
// 用recyler方式实现
// todo 再算算时间吧
class RaceMatchItemView : ConstraintLayout {

    private val totalTime: Long = 10 * 1000L  // 动画的总时间
    private val fastSpeed: Float = 1f  //通过一个pixel需要的时间
    private val slowSpeed: Float = 1000f  // 最慢速度
    private val itemHeight = U.getDisplayUtils().dip2px(100f)

    private var target = 0  // 最终停的item

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val wheelView: WheelView
    var slotMachineAdapter: SlotMachineAdapter? = null

    //滚动的监听器
    val scrolledListener: OnWheelScrollListener = object : OnWheelScrollListener {
        override fun onScrollingFinished(wheel: WheelView?) {
        }

        override fun onScrollingStarted(wheel: WheelView?) {
        }
    }

    init {
        View.inflate(context, R.layout.race_match_view_item_layout, this)

        wheelView = this.findViewById(R.id.wheel_view)
    }

    fun setData() {
        // 测试数据
        var dataList = ArrayList<UserInfoModel>()
        for (i in 0..5) {
            dataList.add(MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().myUserInfo))
        }
        target = 3

        // 初始化结束
        slotMachineAdapter = SlotMachineAdapter(dataList)
        wheelView.viewAdapter = slotMachineAdapter
        wheelView.addScrollingListener(scrolledListener)
        wheelView.visibleItems = 1
        wheelView.isCyclic = true
        wheelView.isEnabled = false
        wheelView.setDrawShadows(false)

//todo 开始滚吧
//        postDelayed({
//            wheelView.scroll(50, 5000)
//        }, 1000)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    inner class SlotMachineAdapter(val dataList: ArrayList<UserInfoModel>) : AbstractWheelAdapter() {

        override fun getItemsCount(): Int {
            return dataList.size
        }

        override fun getItem(index: Int, convertView: View?, parent: ViewGroup?): View {
            val model = dataList[index]
            val view = if (convertView == null) {
                View.inflate(context, R.layout.race_match_item_layout, null)
            } else {
                convertView
            }
            MyLog.d("SlotMachineAdapter", "getItem parent=$parent index = $index,  view.layoutParams = ${view.layoutParams}")
            val avatarIv: SimpleDraweeView = view.findViewById(R.id.avatar_iv)
            val nameTv: ExTextView = view.findViewById(R.id.name_tv)
            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model.avatar)
                    .setBorderColor(Color.WHITE)
                    .setBorderWidth(2.dp().toFloat())
                    .setCircle(true)
                    .build())
            nameTv.text = model.nicknameRemark
            return view
        }
    }
}