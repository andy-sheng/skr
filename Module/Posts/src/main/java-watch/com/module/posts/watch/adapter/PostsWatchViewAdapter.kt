package com.module.posts.watch.adapter

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.log.MyLog
import com.module.posts.R
import com.module.posts.watch.model.PostsWatchModel
import com.module.posts.watch.viewholder.PostsWatchViewHolder
import java.util.*

class PostsWatchViewAdapter(val listener: PostsWatchListener) : RecyclerView.Adapter<PostsWatchViewHolder>() {

    var mDataList = ArrayList<PostsWatchModel>()

    val NO_PLAY_AUDIO = 0     // 未播放
    val PLAY_POSTS_AUDIO = 1    // 播放音频
    val PLAY_POSTS_COMMENT_AUDIO = 2  // 播放评论音频

    var mCurrentPlayModel: PostsWatchModel? = null   // 播放的model
    var mCurrentPlayPosition = -1    //播放的位置
    var playStatus = NO_PLAY_AUDIO   // 0代表未播放  1播放帖子音频  2播放评论音频

    val mTag = "PostsWatchViewAdapter"
    private val uiHanlder = Handler(Looper.getMainLooper())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsWatchViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_watch_view_item_layout, parent, false)
        return PostsWatchViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: PostsWatchViewHolder, position: Int) {

    }

    override fun onBindViewHolder(holder: PostsWatchViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            holder.bindData(position, mDataList[position])
        } else {
            // 局部刷新
            payloads.forEach { refreshType ->
                if (refreshType is Int) {
                    when (refreshType) {
                        REFRESH_POSTS_PLAY -> {
                            MyLog.d(mTag, "onBindViewHolder REFRESH_POSTS_PLAY refreshType = $refreshType")
                            if (mCurrentPlayModel == mDataList[position]) {
                                if (playStatus == PLAY_POSTS_AUDIO) {
                                    MyLog.d(mTag, "onBindViewHolder startAudioPlay refreshType = $refreshType")
                                    holder.startAudioPlay()
                                    holder.stopCommentAudioPlay()
                                } else if (playStatus == PLAY_POSTS_COMMENT_AUDIO) {
                                    MyLog.d(mTag, "onBindViewHolder startCommentAudioPlay refreshType = $refreshType")
                                    holder.startCommentAudioPlay()
                                    holder.stopAudioPlay()
                                }
                            } else {
                                holder.stopAudioPlay()
                                holder.stopCommentAudioPlay()
                            }
                        }
                        REFRESH_POSTS_LIKE -> {
                            holder.refreshLikes()
                        }
                        REFRESH_POSTS_RED_PKG -> {
                            holder.refreshRedPkg()
                        }
                        REFRESH_POSTS_COMMENT_LIKE -> {
                            holder.refreshCommentLike()
                        }
                    }
                }
            }
        }
    }

    fun startOrPauseAudio(pos: Int, model: PostsWatchModel?, isComment: Boolean) {
        if (playStatus != NO_PLAY_AUDIO && mCurrentPlayModel == model && (isComment == (playStatus == PLAY_POSTS_COMMENT_AUDIO))) {
            // 数据和播放类型一致
            stopPlay()
        } else {
            // 数据改变或者播放的类型不一致了
            var lastPos: Int? = null
            if (mCurrentPlayModel != model) {
                mCurrentPlayModel = model
                lastPos = mCurrentPlayPosition
                mCurrentPlayPosition = pos
            }
            playStatus = if (isComment) {
                PLAY_POSTS_COMMENT_AUDIO
            } else {
                PLAY_POSTS_AUDIO
            }
            notifyItemChanged(pos, REFRESH_POSTS_PLAY)
            lastPos?.let {
                uiHanlder.post { notifyItemChanged(it, REFRESH_POSTS_PLAY) }
            }
        }
    }

    fun stopPlay() {
        update(mCurrentPlayPosition, mCurrentPlayModel, REFRESH_POSTS_PLAY)
        // 重置数据
        mCurrentPlayPosition = -1
        mCurrentPlayModel = null
    }

    fun update(position: Int, model: PostsWatchModel?, refreshType: Int) {
        if (mDataList.isNotEmpty()) {
            if (position in 0 until mDataList.size && mDataList[position] == model) {
                // 位置是对的
                notifyItemChanged(position, refreshType)
                return
            } else {
                update(model, refreshType)
            }
        } else {
            mCurrentPlayModel = null
            mCurrentPlayPosition = -1
        }
    }

    private fun update(model: PostsWatchModel?, refreshType: Int) {
        // 位置是错的
        for (i in 0 until mDataList.size) {
            if (mDataList[i] == model) {
                notifyItemChanged(i, refreshType)
                return
            }
        }
    }

    companion object {
        const val REFRESH_POSTS_PLAY = 0  // 局部刷新帖子播放
        const val REFRESH_POSTS_LIKE = 1  // 局部刷新帖子喜欢
        const val REFRESH_POSTS_RED_PKG = 2  // 局部刷新帖子红包状态
        const val REFRESH_POSTS_COMMENT_LIKE = 3  // 局部刷新帖子精彩评论喜欢
    }
}

interface PostsWatchListener {
    fun onClickPostsAvatar(position: Int, model: PostsWatchModel?)  // 发帖头像
    fun onClickPostsMore(position: Int, model: PostsWatchModel?)    // 发帖更多
    fun onClickPostsAudio(position: Int, model: PostsWatchModel?)   // 发帖音频
    fun onClickPostsImage(position: Int, model: PostsWatchModel?, index: Int, url: String?)   // 发帖图片

    fun onClickPostsRedPkg(position: Int, model: PostsWatchModel?)  // 发帖红包
    fun onClickPostsTopic(position: Int, model: PostsWatchModel?)   // 发帖话题
    fun onClickPostsComment(position: Int, model: PostsWatchModel?) // 发帖评论
    fun onClickPostsLike(position: Int, model: PostsWatchModel?)    // 发帖点赞
    fun onClickPostsVote(position: Int, model: PostsWatchModel?, index: Int)  // 发帖投票

    fun onClickCommentAvatar(position: Int, model: PostsWatchModel?)  // 发帖精彩评论昵称
    fun onClickCommentLike(position: Int, model: PostsWatchModel?)    // 发帖精彩评论点赞
    fun onClickCommentAudio(position: Int, model: PostsWatchModel?)   // 发帖精彩评论音频
    fun onClickCommentImage(position: Int, model: PostsWatchModel?, index: Int, url: String?)   // 发帖精彩评论图片

}