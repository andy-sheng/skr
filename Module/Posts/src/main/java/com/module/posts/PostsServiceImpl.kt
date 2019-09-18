package com.module.posts

import android.content.Context
import android.support.v4.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.module.RouterConstants
import com.module.posts.watch.PostsWatchFragment
import com.module.post.IPostModuleService

@Route(path = RouterConstants.SERVICE_POSTS, name = "测试服务")
class PostsServiceImpl : IPostModuleService {
    val TAG = "PostServiceImpl"

    override fun getFragment(): Fragment {
        return PostsWatchFragment()
    }

    override fun init(context: Context?) {
    }

}
