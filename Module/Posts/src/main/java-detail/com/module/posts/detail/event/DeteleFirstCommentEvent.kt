package com.module.posts.detail.event

import com.module.posts.detail.model.PostFirstLevelCommentModel

// 二级页删除一级评论
class DeteleFirstCommentEvent(val model: PostFirstLevelCommentModel)