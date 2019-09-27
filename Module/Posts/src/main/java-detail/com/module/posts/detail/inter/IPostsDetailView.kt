package com.module.posts.detail.inter

import com.module.posts.detail.model.PostFirstLevelCommentModel
import com.module.posts.watch.model.PostsWatchModel

interface IPostsDetailView {
    fun showFirstLevelCommentList(list: List<PostFirstLevelCommentModel>)
    fun showLikePostsResulet()
    fun showLikeFirstLevelCommentResult(postFirstLevelCommentModel: PostFirstLevelCommentModel)
    fun loadMoreError()
    fun loadDetailDelete()
    fun loadDetailError()
    fun addFirstLevelCommentSuccess(model: PostFirstLevelCommentModel)
    fun addSecondLevelCommentSuccess()
    fun addCommetFaild()
    fun showPostsWatchModel(model: PostsWatchModel)
    fun voteSuccess(position: Int)
    fun showRelation(isBlack: Boolean, isFollow: Boolean, isFriend: Boolean)
    fun hasMore(hasMore: Boolean)
    fun deletePostSuccess(success: Boolean)
    fun deleteCommentSuccess(success: Boolean, pos: Int)
}