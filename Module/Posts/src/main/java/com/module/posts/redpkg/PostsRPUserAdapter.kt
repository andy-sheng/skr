package com.module.posts.redpkg

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.component.busilib.view.AvatarView
import com.module.posts.R

class PostsRPUserAdapter : RecyclerView.Adapter<PostsRPUserAdapter.PostsRedPkgViewHolder>() {

    var mDataList = ArrayList<PostsRedPkgUserModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsRedPkgViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_red_pkg_item_layout, parent, false)
        return PostsRedPkgViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: PostsRedPkgViewHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class PostsRedPkgViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)
        private val moneyTv: TextView = item.findViewById(R.id.money_tv)
        private val nameTv: TextView = item.findViewById(R.id.name_tv)

        fun bindData(pot: Int, model: PostsRedPkgUserModel) {
            avatarIv.bindData(model.userModel)
            moneyTv.text = model.redpacketDesc
            nameTv.text = model.userModel?.nicknameRemark
        }
    }
}

