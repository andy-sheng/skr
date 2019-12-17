package com.module.club.manage.create

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.os.MessageQueue
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.image.model.oss.OssImgFactory
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.upload.UploadCallback
import com.common.upload.UploadParams
import com.common.utils.ImageUtils
import com.common.utils.U
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.ex.NoLeakEditText
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.SkrProgressView
import com.component.person.photo.model.PhotoModel
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.R
import com.module.club.homepage.event.ClubInfoChangeEvent
import com.respicker.ResPicker
import com.respicker.activity.ResPickerActivity
import com.respicker.model.ImageItem
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus

// 创建和编辑吧
@Route(path = RouterConstants.ACTIVITY_CREATE_CLUB)
class CreateClubActivity : BaseActivity() {
    lateinit var titlebar: CommonTitleBar
    lateinit var bgIv: ImageView
    lateinit var iconIvBg: ExImageView
    lateinit var iconIv: BaseImageView
    lateinit var divider: View
    lateinit var clubNameEt: NoLeakEditText
    lateinit var clubIntroductionEt: NoLeakEditText
    lateinit var progressView: SkrProgressView
    lateinit var placeHolder: View
    lateinit var textCount: ExTextView


    internal var mImageItemArrayList: MutableList<ImageItem> = java.util.ArrayList()

    private var clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)
    private var clubMemberInfo: ClubMemberInfo? = null // 更改家族资料会带过来

    var from = "create"

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.club_create_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = findViewById(R.id.titlebar)
        bgIv = findViewById(R.id.bg_iv)
        iconIvBg = findViewById(R.id.icon_iv_bg)
        iconIv = findViewById(R.id.icon_iv)
        divider = findViewById(R.id.divider)
        clubNameEt = findViewById(R.id.club_name_et)
        clubIntroductionEt = findViewById(R.id.club_introduction_et)
        progressView = findViewById(R.id.progress_view)
        placeHolder = findViewById(R.id.place_holder)
        textCount = findViewById(R.id.text_count)
        placeHolder.layoutParams.height = U.getDisplayUtils().phoneHeight - U.getDisplayUtils().dip2px(486f)

        Looper.myQueue().addIdleHandler(object : MessageQueue.IdleHandler {
            override fun queueIdle(): Boolean {
                placeHolder.layoutParams.height = U.getDisplayUtils().phoneHeight - U.getDisplayUtils().dip2px(486f) - titlebar.measuredHeight
                placeHolder.requestLayout()
                return false
            }
        })

        clubIntroductionEt.addTextChangedListener(object : TextWatcher {
            var preString = ""
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                preString = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s.toString()
                val length = str.length
                if (length > 200) {
                    val selectIndex = preString.length
                    clubIntroductionEt.setText(preString)
                    clubIntroductionEt.setSelection(selectIndex)
                    textCount.text = "200/200"
                } else {
                    textCount.text = "${s.toString().length}/200"
                }
            }
        })

//        clubNameEt.addTextChangedListener(object : TextWatcher {
//            var preString = ""
//            override fun afterTextChanged(s: Editable?) {
//
//            }
//
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//                preString = s.toString()
//                MyLog.d(TAG, "beforeTextChanged s = $preString")
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                val str = s.toString()
//                val length = U.getStringUtils().getStringLength(str)
//                if (length > 30) {
//                    MyLog.d(TAG, "onTextChanged s = $str")
//                    val selectIndex = preString.length
//                    clubNameEt.setText(preString)
//                    clubNameEt.setSelection(selectIndex)
//                    U.getToastUtil().showShort("不能超过15个汉字或30个英文")
//                }
//            }
//        })

        val from = intent?.getStringExtra("from") ?: "create"
        if (!TextUtils.isEmpty(from)) {
            this.from = from
            if ("change" == from) {
                clubMemberInfo = intent?.getSerializableExtra("clubMemberInfo") as ClubMemberInfo?
                titlebar.rightTextView?.text = "更改"
                // 初始化内容
                FrescoWorker.loadImage(iconIv, ImageFactory.newPathImage(clubMemberInfo?.club?.logo)
                        .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                        .setFailureDrawable(U.app().resources.getDrawable(com.component.busilib.R.drawable.load_img_error))
                        .setLoadingDrawable(U.app().resources.getDrawable(com.component.busilib.R.drawable.loading_place_holder_img))
                        .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_320.w).build())
                        .build())
                clubNameEt.setText("${clubMemberInfo?.club?.name}")
                clubIntroductionEt.setText("${clubMemberInfo?.club?.desc}")
            }
        }

        titlebar.leftTextView.setDebounceViewClickListener { finish() }

        titlebar.rightTextView.setDebounceViewClickListener {
            if ("create" == from) {
                if (mImageItemArrayList == null || mImageItemArrayList.size == 0) {
                    U.getToastUtil().showShort("请添加图片")
                    return@setDebounceViewClickListener
                }

                if (TextUtils.isEmpty(clubNameEt.text.toString().trim())) {
                    U.getToastUtil().showShort("请填写名称")
                    return@setDebounceViewClickListener
                }

                if (TextUtils.isEmpty(clubIntroductionEt.text.toString().trim())) {
                    U.getToastUtil().showShort("请填写简介")
                    return@setDebounceViewClickListener
                }

                editFinish()
            } else if ("change" == from) {
                if (TextUtils.isEmpty(clubNameEt.text.toString().trim())) {
                    U.getToastUtil().showShort("请填写名称")
                    return@setDebounceViewClickListener
                }

                if (TextUtils.isEmpty(clubIntroductionEt.text.toString().trim())) {
                    U.getToastUtil().showShort("请填写简介")
                    return@setDebounceViewClickListener
                }
                changeFinish()
            }

        }

        iconIvBg.setDebounceViewClickListener {
            goAddPhotoFragment()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == ResPickerActivity.REQ_CODE_RES_PICK) {
            val imageItems = ResPicker.getInstance().selectedImageList
            mImageItemArrayList = imageItems

            if (imageItems != null && imageItems.size > 0) {
                FrescoWorker.loadImage(iconIv,
                        ImageFactory.newPathImage(imageItems.get(0).getPath())
                                .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                                .setFailureDrawable(U.app().resources.getDrawable(com.component.busilib.R.drawable.load_img_error))
                                .setLoadingDrawable(U.app().resources.getDrawable(com.component.busilib.R.drawable.loading_place_holder_img))
                                .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_320.w).build())
                                .build())
            }
        }
    }

    internal fun execUploadPhoto(photoModel: PhotoModel, callback: UploadCallback?) {
        MyLog.d(TAG, "execUploadPhoto photoModel=$photoModel")
        val uploadTask = UploadParams.newBuilder(photoModel.localPath)
                .setNeedCompress(true)
                .setNeedMonitor(true)
                .setFileType(UploadParams.FileType.picture)
                .startUploadAsync(object : UploadCallback {
                    override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {
                        callback?.onProgressNotInUiThread(currentSize, totalSize)
                    }

                    override fun onSuccessNotInUiThread(url: String) {
                        MyLog.d(TAG, "上传成功 url=$url")
                        callback?.onSuccessNotInUiThread(url)
                    }

                    override fun onFailureNotInUiThread(msg: String) {
                        MyLog.d(TAG, "上传失败 msg=$msg")
                        callback?.onFailureNotInUiThread(msg)
                    }
                })
    }

    private fun goAddPhotoFragment() {
        ResPicker.getInstance().params = ResPicker.newParamsBuilder()
                .setMultiMode(true)
                .setShowCamera(true)
                .setIncludeGif(false)
                .setCrop(false)
                .setSelectLimit(1)
                .build()

        ResPickerActivity.open(this, ArrayList<ImageItem>(mImageItemArrayList))
    }

    private fun editFinish() {
        progressView.visibility = View.VISIBLE

        val photoModel = PhotoModel()
        photoModel.localPath = mImageItemArrayList[0].getPath()
        photoModel.status = PhotoModel.STATUS_WAIT_UPLOAD
        execUploadPhoto(photoModel, object : UploadCallback {
            override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {

            }

            override fun onSuccessNotInUiThread(url: String?) {
                photoModel.status = PhotoModel.STATUS_SUCCESS
                photoModel.picPath = url

                createClub(photoModel)
            }

            override fun onFailureNotInUiThread(msg: String?) {
                progressView.visibility = View.GONE
                photoModel.status = PhotoModel.STATUS_FAILED
                U.getToastUtil().showShort(msg)
            }
        })

        U.getKeyBoardUtils().hideSoftInputKeyBoard(this)
    }

    private fun changeFinish() {
        var hasLogoChange = true
        if (mImageItemArrayList == null || mImageItemArrayList.size == 0) {
            hasLogoChange = false
        }
        var hasNameChange = true
        if (clubMemberInfo?.club?.name?.equals(clubNameEt.text.toString().trim()) == true) {
            hasNameChange = false
        }
        var hasContentChange = true
        if (clubMemberInfo?.club?.desc?.equals(clubIntroductionEt.text.toString().trim()) == true) {
            hasContentChange = false
        }

        if (hasLogoChange || hasNameChange || hasContentChange) {
            // 编辑过了
            if (hasLogoChange) {
                progressView.visibility = View.VISIBLE

                val photoModel = PhotoModel()
                photoModel.localPath = mImageItemArrayList[0].getPath()
                photoModel.status = PhotoModel.STATUS_WAIT_UPLOAD
                execUploadPhoto(photoModel, object : UploadCallback {
                    override fun onProgressNotInUiThread(currentSize: Long, totalSize: Long) {

                    }

                    override fun onSuccessNotInUiThread(url: String?) {
                        photoModel.status = PhotoModel.STATUS_SUCCESS
                        photoModel.picPath = url

                        setClubInfo(url, clubNameEt.text.toString().trim(), clubIntroductionEt.text.toString().trim())
                    }

                    override fun onFailureNotInUiThread(msg: String?) {
                        progressView.visibility = View.GONE
                        photoModel.status = PhotoModel.STATUS_FAILED
                        U.getToastUtil().showShort(msg)
                    }
                })

                U.getKeyBoardUtils().hideSoftInputKeyBoard(this)
            } else {
                setClubInfo(null, clubNameEt.text.toString().trim(), clubIntroductionEt.text.toString().trim())
            }
        }
    }

    private fun setClubInfo(url: String?, name: String?, content: String?) {
        launch {
            val map = HashMap<String, Any?>()
            map["clubID"] = clubMemberInfo?.club?.clubID ?: 0
            if (!TextUtils.isEmpty(url)) {
                map["logo"] = url
            }
            if (clubMemberInfo?.club?.name?.equals(clubNameEt.text.toString().trim()) == false) {
                map["name"] = clubNameEt.text.toString().trim()
            }
            if (clubMemberInfo?.club?.desc?.equals(clubIntroductionEt.text.toString().trim()) == false) {
                map["desc"] = clubIntroductionEt.text.toString().trim()
            }

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("setNotice", ControlType.CancelThis)) {
                clubServerApi.editClubInfo(body)
            }

            if (result.errno == 0) {
                EventBus.getDefault().post(ClubInfoChangeEvent())
                U.getToastUtil().showShort("家族资料审核中")
                finish()
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

    private fun createClub(photoModel: PhotoModel) {
        launch {
            val map = mutableMapOf("logo" to photoModel.picPath, "name" to clubNameEt.text.toString(), "desc" to clubIntroductionEt.text.toString())
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("createClub", ControlType.CancelThis)) {
                clubServerApi.createClub(body)
            }

            if (result.errno == 0) {
                U.getToastUtil().showShort("已提交创建家族申请")
                progressView.visibility = View.GONE
                finish()
            } else {
                progressView.visibility = View.GONE
                U.getToastUtil().showShort(result.errmsg)
            }

        }
    }

    override fun destroy() {
        super.destroy()
        U.getKeyBoardUtils().hideSoftInputKeyBoard(this)
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}