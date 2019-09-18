package com.module.posts.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.component.busilib.view.AvatarView
import com.module.posts.R
import com.module.posts.watch.model.PostsWatchModel

class PostsWatchViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)
    val nicknameTv: TextView = item.findViewById(R.id.nickname_tv)
    val timeTv: TextView = item.findViewById(R.id.time_tv)
    val moreIv: ImageView = item.findViewById(R.id.more_iv)

    var mPosition = -1
    var mModel: PostsWatchModel? = null

    init {

    }

    fun bindData(pos: Int, model: PostsWatchModel) {
        mPosition = pos
        mModel = model

        // todo 测试玩一下
        avatarIv.bindData(MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().myUserInfo))
        nicknameTv.text = MyUserInfoManager.getInstance().nickName
        timeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(System.currentTimeMillis(),System.currentTimeMillis())
    }
}