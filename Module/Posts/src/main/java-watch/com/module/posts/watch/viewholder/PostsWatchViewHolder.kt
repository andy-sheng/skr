package com.module.posts.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.common.utils.dp
import com.component.busilib.view.AvatarView
import com.module.posts.R
import com.module.posts.watch.model.PostsWatchModel
import com.module.posts.view.ExpandTextView
import com.module.posts.view.PostsNineGridLayout

// post_watch_view_item_layout
class PostsWatchViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)
    val nicknameTv: TextView = item.findViewById(R.id.nickname_tv)
    val timeTv: TextView = item.findViewById(R.id.time_tv)
    val moreIv: ImageView = item.findViewById(R.id.more_iv)
    val nineGridVp: PostsNineGridLayout = item.findViewById(R.id.nine_grid_vp)
    val content: ExpandTextView = item.findViewById(R.id.content)

    var pos = -1
    var model: PostsWatchModel? = null

    var imageClickListener: ((pos:Int,model: PostsWatchModel?, index: Int, url: String?) -> Unit)? = null

    init {
        nineGridVp.clickListener = { i, url, _ ->
            imageClickListener?.invoke(pos,model, i, url)
        }
        content.setListener(object : ExpandTextView.ExpandListener {
            override fun onClickExpand(isExpand: Boolean) {
                model?.isExpend = isExpand
            }
        })
    }

    fun bindData(pos: Int, model: PostsWatchModel) {
        this.pos = pos
        this.model = model

        // todo 测试玩一下
        avatarIv.bindData(MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().myUserInfo))
        nicknameTv.text = MyUserInfoManager.getInstance().nickName
        timeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(System.currentTimeMillis(), System.currentTimeMillis())

        nineGridVp.setUrlList(model.imageList)
        content.initWidth(U.getDisplayUtils().screenWidth - 20.dp())
        content.maxLines = 3
        if (!model?.isExpend) {
            content.setCloseText("茫茫的长白大山，浩瀚的原始森林，大山脚下，原始森林环抱中散落着几十户人家的一个小山村，茅草房，对面炕，烟筒立在屋后边。在村东头有一个独立的房子，那就是青年点窗前有一道小溪流过。学子在这里吃饭，由这里出发每天随社员去地里干活。干的活要么上山伐 树，抬树，要么砍柳树毛子开荒种地。在山里，可听那吆呵声：“顺山倒了！”放树谨防回头棒！ 树上的枯枝打到别的树上再蹦回来，这回头棒打人最厉害.")
        } else {
            content.setExpandText("茫茫的长白大山，浩瀚的原始森林，大山脚下，原始森林环抱中散落着几十户人家的一个小山村，茅草房，对面炕，烟筒立在屋后边。在村东头有一个独立的房子，那就是青年点窗前有一道小溪流过。学子在这里吃饭，由这里出发每天随社员去地里干活。干的活要么上山伐 树，抬树，要么砍柳树毛子开荒种地。在山里，可听那吆呵声：“顺山倒了！”放树谨防回头棒！ 树上的枯枝打到别的树上再蹦回来，这回头棒打人最厉害.")
        }
    }
}