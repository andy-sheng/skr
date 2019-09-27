package com.module.posts.detail.event

import com.module.posts.detail.model.PostsSecondLevelCommentModel

// 从详情出来最新的PostsWatchModel
class DeteleSecondCommentEvent(val model: PostsSecondLevelCommentModel?, val firstLevelCommentID: Int)