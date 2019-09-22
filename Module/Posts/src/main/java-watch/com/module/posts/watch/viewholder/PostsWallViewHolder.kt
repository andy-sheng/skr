package com.module.posts.watch.viewholder

import android.view.View
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.component.busilib.view.AvatarView
import com.module.posts.R
import com.module.posts.watch.adapter.PostsWatchListener
import com.module.posts.watch.model.PostsWatchModel

class PostsWallViewHolder (item: View, listener: PostsWatchListener) : PostsViewHolder(item, listener){

    val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)
    val nicknameTv: TextView = item.findViewById(R.id.nickname_tv)
    val timeTv: TextView = item.findViewById(R.id.time_tv)

    init {
        avatarIv.setDebounceViewClickListener { listener.onClickCommentAvatar(pos, mModel) }
    }

    override fun bindData(pos: Int, model: PostsWatchModel) {
        super.bindData(pos, model)

        if (mModel?.user != null) {
            avatarIv.bindData(mModel?.user!!)
            nicknameTv.text = mModel?.user?.nicknameRemark
        } else {
            MyLog.e("PostsWatchViewHolder", "bindData error pos = $pos, model = $model")
        }

        mModel?.posts?.let {
            timeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(it.createdAt, System.currentTimeMillis())
        }
    }
}