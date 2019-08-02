package com.module.feeds.detail.inter

import com.module.feeds.detail.model.FirstLevelCommentModel

interface IFirstLevelCommentView {
    fun noMore(isEmpty: Boolean)
    fun updateList(list: List<FirstLevelCommentModel>?)
    fun likeFinish(firstLevelCommentModel: FirstLevelCommentModel, position: Int, like: Boolean)
    fun finishLoadMore()
    fun showNum(count: Int)
}