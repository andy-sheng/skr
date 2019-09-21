package com.module.posts.watch.view

import android.support.v4.app.FragmentActivity
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.component.person.view.RequestCallBack
import com.module.post.IPersonPostsWall
import com.module.posts.more.PostsMoreDialogView
import com.module.posts.watch.model.PostsWatchModel
import kotlinx.coroutines.launch

class PersonPostsWatchView(activity: FragmentActivity, var userInfoModel: UserInfoModel, val callback: RequestCallBack) : BasePostsWatchView(activity, TYPE_POST_PERSON), IPersonPostsWall {

    override fun selected() {
        super.selected()
        initPostsList(false)
    }

    override fun unselected(reason: Int) {
        super.unselected(reason)
    }

    override fun setUserInfoModel(any: Any?) {
        val model = any as UserInfoModel?
        model?.let { userInfoModel = model }
    }

    override fun isHasMore(): Boolean {
        return hasMore
    }

    override fun getPosts(flag: Boolean) {
        initPostsList(flag)
    }

    override fun destroy() {
        super.destory()
    }

    override fun onClickMore(position: Int, model: PostsWatchModel?) {
        model?.let {
            postsMoreDialogView?.dismiss(false)
            postsMoreDialogView = PostsMoreDialogView(activity, PostsMoreDialogView.FROM_POSTS_PERSON, it)
            if (userInfoModel.userId == MyUserInfoManager.getInstance().uid.toInt()) {
                postsMoreDialogView?.apply {
                    reportTv.text = "删除"
                    reportTv.setOnClickListener(object : DebounceViewClickListener() {
                        override fun clickValid(v: View?) {
                            //todo 补全删除逻辑
                            postsMoreDialogView?.dismiss(false)
                        }
                    })
                }
            }
            postsMoreDialogView?.showByDialog(true)
        }
    }

    override fun initPostsList(flag: Boolean): Boolean {
        if (!flag && mHasInitData) {
            // 不一定要刷新
            return false
        }

        getPersonPosts(0, true)
        return true
    }

    override fun getMorePosts() {
        if (hasMore) {
            getPersonPosts(mOffset, false)
        } else {
            U.getToastUtil().showShort("没有更多了")
        }
    }

    private fun getPersonPosts(off: Int, isClear: Boolean) {
        launch {
            val result = subscribe(RequestControl("getPersonPosts", ControlType.CancelThis)) {
                if (userInfoModel.userId == MyUserInfoManager.getInstance().uid.toInt()) {
                    postsWatchServerApi.getHomePagePostsList(off, mCNT, MyUserInfoManager.getInstance().uid, userInfoModel.userId.toLong(), 1)
                } else {
                    postsWatchServerApi.getHomePagePostsList(off, mCNT, MyUserInfoManager.getInstance().uid, userInfoModel.userId.toLong(), 2)
                }
            }
            if (result.errno == 0) {
                mHasInitData = true
                val list = JSON.parseArray(result.data.getString("details"), PostsWatchModel::class.java)
                mOffset = result.data.getIntValue("offset")
                hasMore = result.data.getBoolean("hasMore")
                addWatchPosts(list, isClear)
            } else {
                if (result.errno == -2) {
                    U.getToastUtil().showShort("网络出错了，请检查网络后重试")
                }
            }

            callback.onRequestSucess(hasMore)
        }
    }
}