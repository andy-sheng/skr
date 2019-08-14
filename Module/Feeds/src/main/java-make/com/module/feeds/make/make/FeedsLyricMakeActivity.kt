package com.module.feeds.make.make

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.log.MyLog
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.component.lyrics.LyricsManager
import com.dialog.view.TipsDialogView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.make.FeedsMakeModel
import com.module.feeds.make.sFeedsMakeModelHolder


@Route(path = RouterConstants.ACTIVITY_FEEDS_LYRIC_MAKE)
class FeedsLyricMakeActivity : BaseActivity() {

    var mFeedsMakeModel: FeedsMakeModel? = null

    lateinit var titleBar: CommonTitleBar
    lateinit var lyricRv: RecyclerView
    lateinit var lyricAdapter: FeedsLyricMakeAdapter


    var originLyric: String? = ""
    var originLyricThisTime: String? = ""

    var tipsDialogView: TipsDialogView? = null

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_lyric_make_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mFeedsMakeModel = sFeedsMakeModelHolder
        sFeedsMakeModelHolder = null
        titleBar = findViewById(R.id.title_bar)
        lyricRv = findViewById(R.id.lyric_rv)
        titleBar.leftImageButton.setOnClickListener { finish() }
        lyricRv.layoutManager = LinearLayoutManager(this)
        lyricAdapter = FeedsLyricMakeAdapter()
        lyricRv.adapter = lyricAdapter
        lyricRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    U.getKeyBoardUtils().hideSoftInputKeyBoard(this@FeedsLyricMakeActivity)
                }
            }
        })
        titleBar.leftImageButton.setOnClickListener {
          finishPage()
        }
        titleBar.rightCustomView.setOnClickListener {
            if (lyricAdapter.songName.isNullOrEmpty()) {
                U.getToastUtil().showShort("歌曲名不能为空")
                return@setOnClickListener
            }
            // 判断歌词是否有变化
            //  尝试写入伴奏歌词
            lyricAdapter.getData().forEach { lyricItem ->
                if (lyricItem.newContent.length != lyricItem.content.length) {
                    U.getToastUtil().showShort("歌词字数请与原歌词保持一致")
                    return@setOnClickListener
                }
            }
            // 判断歌词是否有变化
            //  尝试写入伴奏歌词
            if (mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader == null) {
                if (mFeedsMakeModel?.songModel?.songTpl?.lrcTs?.isNullOrEmpty() == false) {
                    LyricsManager.loadStandardLyric(mFeedsMakeModel?.songModel?.songTpl?.lrcTs)
                            .subscribe({
                                mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader = it
                                lyricAdapter.getData().forEachIndexed { index, lyricItem ->
                                    mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader?.lrcLineInfos?.get(index)?.lineLyrics = lyricItem.newContent
                                }
                            }, { MyLog.e(TAG, it) })
                }
            } else {
                lyricAdapter.getData().forEachIndexed { index, lyricItem ->
                    mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader?.lrcLineInfos?.get(index)?.lineLyrics = lyricItem.newContent
                }
            }

            // 尝试写入清唱歌词
            val sb = StringBuilder()
            lyricAdapter.getData().forEachIndexed { index, lyricItem ->
                sb.append(lyricItem.newContent).append("\n")
            }
            mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr = sb.toString()
            if (originLyric != mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr) {
                mFeedsMakeModel?.hasChangeLyric = true
            }
            if (originLyricThisTime != mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr) {
                mFeedsMakeModel?.hasChangeLyricOrSongNameThisTime = true
            }
            if(lyricAdapter.songName!=mFeedsMakeModel?.songModel?.songTpl?.songNameChange){
                mFeedsMakeModel?.songModel?.songTpl?.songNameChange = lyricAdapter.songName
                mFeedsMakeModel?.hasChangeLyricOrSongNameThisTime = true
            }

            sFeedsMakeModelHolder = mFeedsMakeModel
            setResult(Activity.RESULT_OK)
            finish()
        }
        val list = ArrayList<LyricItem>()

        // 原唱歌词
        val lrcTs = mFeedsMakeModel?.songModel?.songTpl?.lrcTs
        if (!TextUtils.isEmpty(lrcTs)) {
            LyricsManager.loadStandardLyric(lrcTs)
                    .subscribe({
                        MyLog.d(TAG, "loadStandardLyric")
                        val size = it?.lrcLineInfos?.size
                        if (size != null && size > 0) {
                            val map = it?.lrcLineInfos
                            map?.forEach { it ->
                                val lyric = it.value.lineLyrics
                                originLyric = originLyric + "\n" + lyric
                                val item = LyricItem(lyric)
                                item.startTs = it.value.startTime
                                item.endTs = it.value.endTime
                                list.add(item)
                            }
                            fillNewContent(list)
                        }
                    }, {
                        MyLog.e(TAG, it)
                    })
        } else {
            val lrcTxt = mFeedsMakeModel?.songModel?.songTpl?.lrcTxt
            LyricsManager.loadGrabPlainLyric(lrcTxt)
                    .subscribe({ lrcTxtStr ->
                        originLyric = lrcTxtStr
                        if (!TextUtils.isEmpty(lrcTxtStr)) {
                            lrcTxtStr?.split("\n")?.forEach {
                                if (!TextUtils.isEmpty(it)) {
                                    val item = LyricItem(it)
                                    list.add(item)
                                }
                            }
                            fillNewContent(list)
                        }
                    }, {
                        MyLog.e(TAG, it)
                    })
        }
    }

    private fun fillNewContent(list: ArrayList<LyricItem>) {
        MyLog.d(TAG, "fillNewContentlist = $list")
        if (mFeedsMakeModel?.withBgm == true) {
            val size = mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader?.lrcLineInfos?.size
            if (size != null && size > 0) {
                val map = mFeedsMakeModel?.songModel?.songTpl?.lrcTsReader?.lrcLineInfos
                var index = 0
                map?.forEach {
                    val lyric = it.value.lineLyrics
                    originLyricThisTime = originLyricThisTime + "\n" + lyric
                    if (index < list.size) {
                        list[index].newContent = lyric
                    }
                    index++
                }
            }
        } else {
            val lrcTxtStr = mFeedsMakeModel?.songModel?.songTpl?.lrcTxtStr
            if (!TextUtils.isEmpty(lrcTxtStr)) {
                originLyricThisTime = lrcTxtStr
                var index = 0
                lrcTxtStr?.split("\n")?.forEach { s ->
                    if (index < list.size) {
                        list[index].newContent = s
                    }
                    index++
                }
            }
        }
        // 歌曲名不用展示原来的
        MyLog.d(TAG, "fillNewContentlist update")
        lyricAdapter.setData(mFeedsMakeModel?.songModel?.getDisplayName()?:"", list)
    }

    override fun onResume() {
        super.onResume()
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun onBackPressed() {
        finishPage()
    }

    private fun finishPage() {
        U.getKeyBoardUtils().hideSoftInputKeyBoard(this)
        // 尝试写入清唱歌词
        val sb = StringBuilder()
        lyricAdapter.getData().forEachIndexed { index, lyricItem ->
            sb.append(lyricItem.newContent).append("\n")
        }
        var hasChange = false
        if (originLyricThisTime != sb.toString()) {
            hasChange = true
        }
        if (lyricAdapter.songName != mFeedsMakeModel?.songModel?.songTpl?.songNameChange) {
            hasChange = true
        }
        if (hasChange) {
            tipsDialogView?.dismiss()
            tipsDialogView = TipsDialogView.Builder(this@FeedsLyricMakeActivity)
                    .setConfirmTip("退出")
                    .setCancelTip("取消")
                    .setCancelBtnClickListener {
                        tipsDialogView?.dismiss()
                    }
                    .setMessageTip("确定要放弃已修改的内容退出么?")
                    .setConfirmBtnClickListener {
                        finish()
                    }
                    .build()
            tipsDialogView?.showByDialog()
        } else {
            finish()
        }
    }
}


fun openLyricMakeActivity(mFeedsMakeModel: FeedsMakeModel? = null, activity: BaseActivity) {
    sFeedsMakeModelHolder = mFeedsMakeModel
    activity.startActivityForResult(Intent(activity, FeedsLyricMakeActivity::class.java), 101)
}