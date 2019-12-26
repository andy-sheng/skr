package com.component.person.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alibaba.android.arouter.launcher.ARouter
import com.common.core.avatar.AvatarUtils
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.person.photo.model.PhotoModel
import com.facebook.drawee.view.SimpleDraweeView
import com.module.RouterConstants

// 守护的view
class GuardView : ConstraintLayout {

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    val recyclerView: RecyclerView
    val adapter = GuardAdapter()

    var userID: Int = 0

    var clickListener: ((model: UserInfoModel?) -> Unit)? = null

    init {
        View.inflate(context, R.layout.guard_view_layout, this)
        recyclerView = this.findViewById(R.id.recycler_view)

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        adapter.mOnClickItemListener = { position, model ->
            if (position == 3) {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_GUARD_LIST)
                        .withInt("userID", userID)
                        .navigation()
            } else {
                clickListener?.invoke(model)
            }

        }
    }

    fun bindData(userID: Int, list: List<UserInfoModel>?, total: Int) {
        this.userID = userID
        adapter.mDataList.clear()
        adapter.total = total
        if (!list.isNullOrEmpty()) {
            adapter.mDataList.addAll(list)
        }
        adapter.notifyDataSetChanged()
    }

}

class GuardAdapter : RecyclerView.Adapter<GuardAdapter.GuardViewHolder>() {

    var total: Int = 0
    var mDataList = ArrayList<UserInfoModel>()
    var mOnClickItemListener: ((position: Int, model: UserInfoModel?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.guard_view_item_layout, parent, false)
        return GuardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun onBindViewHolder(holder: GuardViewHolder, position: Int) {
        if (position == 3) {
            holder.bindData(position, null, "${total}人")
        } else {
            if (!mDataList.isNullOrEmpty() && position < mDataList.size) {
                holder.bindData(position, mDataList[position], null)
            } else {
                holder.bindData(position, null, "守护")
            }
        }
    }

    inner class GuardViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        var mPos = -1
        var mModel: UserInfoModel? = null

        val avatarIv: SimpleDraweeView = item.findViewById(R.id.avatar_iv)
        val emptyTv: ExTextView = item.findViewById(R.id.empty_tv)
        val avatarBg: ImageView = item.findViewById(R.id.avatar_bg)

        init {
            item.setDebounceViewClickListener {
                mOnClickItemListener?.invoke(mPos, mModel)
            }
        }

        fun bindData(position: Int, model: UserInfoModel?, text: String?) {
            this.mPos = position
            this.mModel = model

            emptyTv.text = text
            if (model == null) {
                avatarIv.visibility = View.GONE
                emptyTv.visibility = View.VISIBLE
            } else {
                avatarIv.visibility = View.VISIBLE
                emptyTv.visibility = View.GONE

                AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model.avatar)
                        .setCircle(true)
                        .build())
            }
        }
    }
}