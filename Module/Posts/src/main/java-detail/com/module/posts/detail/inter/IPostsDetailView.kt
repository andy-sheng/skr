package com.module.posts.detail.inter

import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.watch.model.PostsWatchModel

interface IPostsDetailView {
    fun showFirstLevelCommentList(list: List<PostFirstLevelCommentModel>, hasMore: Boolean)
    fun showLikePostsResulet()
    fun showLikeFirstLevelCommentResult(postFirstLevelCommentModel: PostFirstLevelCommentModel)
    fun loadMoreError()
    fun loadDetailDelete()
    fun loadDetailError()
    fun addFirstLevelCommentSuccess()
    fun addSecondLevelCommentSuccess()
    fun addCommetFaild()
    fun showPostsWatchModel(model: PostsWatchModel)
}