package com.module.posts.watch.view

import android.support.v4.app.FragmentActivity
import android.view.View
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.component.person.view.RequestCallBack
import com.module.posts.dialog.PostsMoreDialogView
import com.module.posts.watch.model.PostsWatchModel

class PersonPostsWatchView(activity: FragmentActivity, var userInfoModel: UserInfoModel, val callback: RequestCallBack) : BasePostsWatchView(activity, TYPE_POST_PERSON) {

    override fun selected() {
        super.selected()
        initPostsList(false)
    }

    override fun unselected(reason: Int) {
        super.unselected(reason)
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

    }
}