package com.module.posts.detail.inter

import com.module.posts.detail.model.PostFirstLevelCommentModel

interface IPostsDetailView {
    fun showFirstLevelCommentList(list: List<PostFirstLevelCommentModel>, hasMore: Boolean)
    fun showRelation(isBlacked: Boolean, isFollow: Boolean, isFriend: Boolean)
}