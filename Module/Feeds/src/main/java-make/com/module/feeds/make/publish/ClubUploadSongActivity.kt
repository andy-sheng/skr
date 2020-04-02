package com.module.feeds.make.publish

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.view.setDebounceViewClickListener
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.upload.UploadCallback
import com.common.upload.UploadParams
import com.common.utils.U
import com.common.view.ex.NoLeakEditText
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.SkrProgressView
import com.module.RouterConstants
import com.module.club.IClubModuleService
import com.module.feeds.R
import com.module.feeds.make.FeedsMakeServerApi
import com.module.feeds.make.sFeedsMakeModelHolder
import com.module.feeds.songmanage.activity.FeedSongManagerActivity
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

@Route(path = RouterConstants.ACTIVITY_CLUB_UPLOAD_SONG)
class ClubUploadSongActivity : BaseActivity() {
    lateinit var titlebar: CommonTitleBar
    lateinit var songNameEt: NoLeakEditText
    lateinit var singerEt: NoLeakEditText
    lateinit var skrProgressView: SkrProgressView

    private var familyID: Int? = 0

    var category: Int = 0

    var title: String? = null
    var path: String? = null

    val feedsMakeServerApi = ApiManager.getInstance().createService(FeedsMakeServerApi::class.java)

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_upload_song_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        for (ac in U.getActivityUtils().activityList) {
            if (ac is FeedSongManagerActivity) {
                ac.finish()
            }
        }

        titlebar = findViewById(R.id.titlebar)
        songNameEt = findViewById(R.id.song_name_et)
        singerEt = findViewById(R.id.singer_et)
        skrProgressView = findViewById(R.id.progress_view)

        if (intent.hasExtra("familyID")) {
            familyID = intent.getIntExtra("familyID", 0)
        }

        if (intent.hasExtra("category")) {
            category = intent.getIntExtra("category", 0)
        }

        titlebar.leftTextView.setDebounceViewClickListener {
            finish()
        }

        //EFC_UNKNOWN = 0; //初始化
        //EFC_Skr     = 1; //平台选择
        //EFC_Local   = 2; //用户本地上传作品
        if (category == 2) {
            //本地文件
            title = intent.getStringExtra("title")
            path = intent.getStringExtra("path")

            songNameEt.setText(title)
            singerEt.setText(MyUserInfoManager.nickName)
        } else {
            songNameEt.setText(sFeedsMakeModelHolder?.songModel?.getDisplayName())
            singerEt.setText(MyUserInfoManager.nickName)
        }

        titlebar.rightTextView.setDebounceViewClickListener {
            if (TextUtils.isEmpty(songNameEt.text.toString().trim())) {
                U.getToastUtil().showShort("歌曲名为空")
                return@setDebounceViewClickListener
            }

            if (TextUtils.isEmpty(singerEt.text.toString().trim())) {
                U.getToastUtil().showShort("演唱者为空")
                return@setDebounceViewClickListener
            }

            titlebar.rightTextView.isEnabled = false
            skrProgressView.visibility = View.VISIBLE
            if (category == 2) {
                uploadAudio(path!!) {
                    addWork(it!!)
                }
            } else {
                uploadAudio(sFeedsMakeModelHolder?.composeSavePath!!) {
                    addWork(it!!)
                }
            }
        }
    }

    private fun uploadAudio(path: String, call: (String?) -> Unit) {
        UploadParams.newBuilder(path)
                .setFileType(UploadParams.FileType.audioAi)
                .setId(familyID!!)
                .startUploadAsync(object : UploadCallback {
                    override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {
                    }

                    override fun onSuccessNotInUiThread(url: String?) {
                        call.invoke(url)
                    }

                    override fun onFailureNotInUiThread(msg: String?) {
                        launch {
                            U.getToastUtil().showShort("上传失败，稍后重试")
                            titlebar.rightTextView.isEnabled = true
                            skrProgressView.visibility = View.GONE
                        }
                    }
                })
    }

    override fun useEventBus(): Boolean {
        return false
    }

    /**
     * {
    "appVersion": "string",
    "category": "EFC_UNKNOWN",enum EFamily_WorksCategory {
    EFC_UNKNOWN = 0; //初始化
    EFC_Skr     = 1; //平台选择
    EFC_Local   = 2; //用户本地上传作品
    }
    "duration": "string",
    "familyID": 0,
    "songID": 0,
    "songName": "string",
    "worksURL": "string"
    }
     */
    private fun addWork(url: String) {
        val map = HashMap<String, Any>()

        if (category == 2) {
            map["category"] = category
            map["duration"] = 10000
            map["familyID"] = familyID!!
            map["songName"] = songNameEt.text.toString()
            map["worksURL"] = url!!
        } else {
            map["category"] = category
            map["duration"] = sFeedsMakeModelHolder?.recordDuration ?: 0
            map["familyID"] = familyID!!
            map["songID"] = sFeedsMakeModelHolder?.songModel?.songID ?: 0
            map["songName"] = songNameEt.text.toString()
            map["worksURL"] = url!!
        }

        map["artist"] = singerEt.text.toString()

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))

        launch {
            var result = subscribe { feedsMakeServerApi.addClubWork(body) }
            if (result.errno == 0) {
                U.getToastUtil().showShort("发布成功")
                finish()

                val iRankingModeService = ARouter.getInstance().build(RouterConstants.SERVICE_CLUB).navigation() as IClubModuleService
                iRankingModeService.finishClubWorkUpload()
            } else {
                U.getToastUtil().showShort(result.errmsg)
                titlebar.rightTextView.isEnabled = true
                skrProgressView.visibility = View.GONE
            }
        }
    }
}