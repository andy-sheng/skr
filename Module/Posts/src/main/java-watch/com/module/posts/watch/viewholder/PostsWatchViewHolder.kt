package com.module.posts.watch.viewholder

import android.view.View
import android.widget.TextView
import com.common.core.view.setDebounceViewClickListener
import com.common.log.MyLog
import com.common.utils.U
import com.component.busilib.view.AvatarView
import com.component.busilib.view.NickNameView
import com.module.posts.R
import com.module.posts.watch.adapter.PostsWatchListener
import com.module.posts.watch.model.PostsWatchModel
import com.module.posts.watch.view.BasePostsWatchView


// posts_watch_view_item_layout
class PostsWatchViewHolder(item: View, listener: PostsWatchListener, val type: Int) : PostsViewHolder(item, listener) {

    private val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)
    private val nickNameView: NickNameView = item.findViewById(R.id.name_view)
    private val timeTv: TextView = item.findViewById(R.id.time_tv)

    init {
        avatarIv.setDebounceViewClickListener { listener.onClickPostsAvatar(pos, mModel) }
        nickNameView.setDebounceViewClickListener { listener.onClickPostsAvatar(pos, mModel) }
    }

    override fun bindData(pos: Int, model: PostsWatchModel) {
        super.bindData(pos, model)

        if (mModel?.user != null) {
            avatarIv.bindData(mModel?.user!!)
            nickNameView.setHonorText(mModel?.user?.nicknameRemark, mModel?.user?.honorInfo)
        } else {
            MyLog.e("PostsWatchViewHolder", "bindData error pos = $pos, model = $model")
        }

        mModel?.posts?.let {
            timeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(it.createdAt, System.currentTimeMillis())
        }
        if (type == BasePostsWatchView.TYPE_POST_RECOMMEND) {
            // 推荐
            timeTv.visibility = View.GONE
        } else {
            timeTv.visibility = View.VISIBLE
        }
    }
}