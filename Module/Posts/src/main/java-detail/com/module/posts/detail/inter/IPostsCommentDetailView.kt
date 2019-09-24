package com.module.posts.detail.inter

import com.module.posts.detail.model.PostsSecondLevelCommentModel

interface IPostsCommentDetailView {
    fun showSecondLevelCommentList(list: List<PostsSecondLevelCommentModel>, hasMore: Boolean)
    fun loadMoreError()
    fun addSecondLevelCommentSuccess()
    fun addCommetFaild()
    fun getFirstLevelCommentID(): Int
}