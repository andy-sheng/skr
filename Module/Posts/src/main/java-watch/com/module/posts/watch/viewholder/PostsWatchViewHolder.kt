package com.module.posts.watch.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.utils.U
import com.component.busilib.view.AvatarView
import com.module.posts.R
import com.module.posts.watch.model.PostsWatchModel
import com.module.posts.watch.view.PostsNineGridLayout
import kotlin.random.Random

// post_watch_view_item_layout
class PostsWatchViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    val avatarIv: AvatarView = item.findViewById(R.id.avatar_iv)
    val nicknameTv: TextView = item.findViewById(R.id.nickname_tv)
    val timeTv: TextView = item.findViewById(R.id.time_tv)
    val moreIv: ImageView = item.findViewById(R.id.more_iv)
    val nineGridVp: PostsNineGridLayout = item.findViewById(R.id.nine_grid_vp)

    var pos = -1
    var model: PostsWatchModel? = null

    var imageClickListener: ((pos:Int,model: PostsWatchModel?, index: Int, url: String?) -> Unit)? = null

    init {
        nineGridVp.clickListener = { i, url, _ ->
            imageClickListener?.invoke(pos,model, i, url)
        }
    }

    fun bindData(pos: Int, model: PostsWatchModel) {
        this.pos = pos
        this.model = model

        // todo 测试玩一下
        avatarIv.bindData(MyUserInfo.toUserInfoModel(MyUserInfoManager.getInstance().myUserInfo))
        nicknameTv.text = MyUserInfoManager.getInstance().nickName
        timeTv.text = U.getDateTimeUtils().formatHumanableDateForSkrFeed(System.currentTimeMillis(), System.currentTimeMillis())

        nineGridVp.setUrlList(model.imageList)
    }
}