package com.module.feeds.detail.inter

import com.module.feeds.detail.model.FirstLevelCommentModel

interface IFirstLevelCommentView {
    fun noMore()
    fun updateList(list: List<FirstLevelCommentModel>?)
}