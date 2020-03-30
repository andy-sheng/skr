package com.module.posts

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.core.userinfo.model.UserInfoModel
import com.component.person.view.RequestCallBack
import com.module.RouterConstants
import com.module.post.IDynamicPostsView
import com.module.post.IPersonPostsWall
import com.module.posts.watch.PostsWatchFragment
import com.module.post.IPostModuleService
import com.module.posts.watch.view.DynamicPostsWatchView
import com.module.posts.watch.view.PersonPostsWatchView

@Route(path = RouterConstants.SERVICE_POSTS, name = "测试服务")
class PostsServiceImpl : IPostModuleService {
    val TAG = "PostServiceImpl"

    override fun getFragment(): Fragment {
        return PostsWatchFragment()
    }

    override fun getDynamicPostsView(activity: FragmentActivity, type: Int): IDynamicPostsView {
        return DynamicPostsWatchView(activity,type)    }

    override fun getPostsWall(activity: Any?, userInfo: Any?, requestCall: Any?): IPersonPostsWall {
        return PersonPostsWatchView(activity as FragmentActivity, userInfo as UserInfoModel, requestCall as RequestCallBack)
    }

    override fun init(context: Context?) {
    }

}
