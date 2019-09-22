package com.module.posts.watch.viewholder

import android.view.View
import android.widget.TextView
import com.module.posts.R
import com.module.posts.watch.adapter.PostsWatchListener
import com.module.posts.watch.model.PostsModel
import com.module.posts.watch.model.PostsWatchModel

class PostsWallViewHolder(item: View, listener: PostsWatchListener) : PostsViewHolder(item, listener) {

    val unauditTv: TextView = item.findViewById(R.id.unaudit_tv)

    override fun bindData(pos: Int, model: PostsWatchModel) {
        super.bindData(pos, model)

        if (model.posts?.status == PostsModel.EPS_AUDIT_ACCEPT) {
            // 审核成功
            unauditTv.visibility = View.GONE
        } else {
            // 审核失败和未审核都算审核中
            unauditTv.visibility = View.VISIBLE
        }

    }
}