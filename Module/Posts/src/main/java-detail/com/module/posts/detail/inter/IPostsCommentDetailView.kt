package com.module.posts.detail.inter

import com.module.posts.detail.model.PostsSecondLevelCommentModel

interface IPostsCommentDetailView {
    fun showSecondLevelCommentList(list: List<PostsSecondLevelCommentModel>)
    fun loadMoreError()
    fun addSecondLevelCommentSuccess(model: PostsSecondLevelCommentModel)
    fun addCommetFaild()
    fun getFirstLevelCommentID(): Int
    fun hasMore(hasMore: Boolean)
}