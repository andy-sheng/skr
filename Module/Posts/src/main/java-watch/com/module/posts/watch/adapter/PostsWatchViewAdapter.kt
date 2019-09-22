package com.module.posts.watch.adapter

import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.log.MyLog
import com.module.posts.R
import com.module.posts.watch.model.PostsWatchModel
import com.module.posts.watch.view.BasePostsWatchView
import com.module.posts.watch.viewholder.PostsEmptyWallViewHolder
import com.module.posts.watch.viewholder.PostsViewHolder
import com.module.posts.watch.viewholder.PostsWallViewHolder
import com.module.posts.watch.viewholder.PostsWatchViewHolder
import java.util.*

class PostsWatchViewAdapter(val type: Int, val listener: PostsWatchListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    var mDataList = ArrayList<PostsWatchModel>()

    var mCurrentPlayModel: PostsWatchModel? = null   // 播放的model
    var mCurrentPlayPosition = -1    //播放的位置
    var playStatus = NO_PLAY_AUDIO   // 0代表未播放  1播放帖子音频  2播放评论音频

    val mTag = "PostsWatchViewAdapter"
    private val uiHanlder = Handler(Looper.getMainLooper())

    companion object {
        const val REFRESH_POSTS_PLAY = 0  // 局部刷新帖子播放
        const val REFRESH_POSTS_LIKE = 1  // 局部刷新帖子喜欢
        const val REFRESH_POSTS_RED_PKG = 2  // 局部刷新帖子红包状态
        const val REFRESH_POSTS_COMMENT_LIKE = 3  // 局部刷新帖子精彩评论喜欢

        const val NO_PLAY_AUDIO = 0       // 未播放
        const val PLAY_POSTS_AUDIO = 1    // 播放音频
        const val PLAY_POSTS_SONG = 2     //播放歌曲音频
        const val PLAY_POSTS_COMMENT_AUDIO = 3  // 播放评论音频
        const val PLAY_POSTS_COMMENT_SONG = 4   // 播放评论歌曲音频

        const val TYPE_POSTS_WATCH = 1
        const val TYPE_POSTS_WALL = 2
        const val TYPE_POSTS_WALL_EMPTY = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_POSTS_WALL_EMPTY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_wall_empty_layout, parent, false)
                PostsEmptyWallViewHolder(view)
            }
            TYPE_POSTS_WALL -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_wall_view_item_layout, parent, false)
                PostsWallViewHolder(view, listener)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_watch_view_item_layout, parent, false)
                PostsWatchViewHolder(view, listener)
            }
        }
    }

    override fun getItemCount(): Int {
        if (type == BasePostsWatchView.TYPE_POST_PERSON && mDataList.size == 0) {
            return 1
        }
        return mDataList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (type == BasePostsWatchView.TYPE_POST_PERSON) {
            if (mDataList.size > 0) {
                TYPE_POSTS_WALL
            } else {
                TYPE_POSTS_WALL_EMPTY
            }
        } else {
            TYPE_POSTS_WATCH
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (holder is PostsViewHolder) {
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
                                    holder.startPlay(playStatus)
                                } else {
                                    holder.stopPlay()
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
    }

    fun startOrPauseAudio(pos: Int, model: PostsWatchModel?, playType: Int) {
        if (playStatus != NO_PLAY_AUDIO && mCurrentPlayModel == model && playType == playStatus) {
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
            playStatus = playType
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
}

interface PostsWatchListener {
    fun onClickPostsAvatar(position: Int, model: PostsWatchModel?)  // 发帖头像
    fun onClickPostsMore(position: Int, model: PostsWatchModel?)    // 发帖更多
    fun onClickPostsAudio(position: Int, model: PostsWatchModel?, isPlaying: Boolean)   // 发帖音频
    fun onClickPostsSong(position: Int, model: PostsWatchModel?, isPlaying: Boolean)   // 发帖歌曲
    fun onClickPostsImage(position: Int, model: PostsWatchModel?, index: Int, url: String?)   // 发帖图片

    fun onClickPostsRedPkg(position: Int, model: PostsWatchModel?)  // 发帖红包
    fun onClickPostsTopic(position: Int, model: PostsWatchModel?)   // 发帖话题
    fun onClickPostsComment(position: Int, model: PostsWatchModel?) // 发帖评论
    fun onClickPostsLike(position: Int, model: PostsWatchModel?)    // 发帖点赞
    fun onClickPostsVote(position: Int, model: PostsWatchModel?, index: Int)  // 发帖投票

    fun onClickCommentAvatar(position: Int, model: PostsWatchModel?)  // 发帖精彩评论昵称
    fun onClickCommentLike(position: Int, model: PostsWatchModel?)    // 发帖精彩评论点赞
    fun onClickCommentAudio(position: Int, model: PostsWatchModel?, isPlaying: Boolean)   // 发帖精彩评论音频
    fun onClickCommentSong(position: Int, model: PostsWatchModel?, isPlaying: Boolean)    // 发帖精彩评论歌曲
    fun onClickCommentImage(position: Int, model: PostsWatchModel?, index: Int, url: String?)   // 发帖精彩评论图片

}