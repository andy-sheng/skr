package com.module.posts.detail.event

import com.module.posts.watch.model.PostsWatchModel

// 从详情出来最新的PostsWatchModel
class PostsDetailEvent(val model: PostsWatchModel?) {
}