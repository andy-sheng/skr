package com.module.club.manage.create

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
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
import com.common.view.ex.NoLeakEditText
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.SkrProgressView
import com.component.person.photo.model.PhotoModel
import com.module.RouterConstants
import com.module.club.ClubServerApi
import com.module.club.R
import com.respicker.ResPicker
import com.respicker.activity.ResPickerActivity
import com.respicker.model.ImageItem
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody

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

    internal var mImageItemArrayList: MutableList<ImageItem> = java.util.ArrayList()

    private var clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)

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

        titlebar.leftTextView.setDebounceViewClickListener { finish() }

        titlebar.rightTextView.setDebounceViewClickListener {
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
                                .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                                .setFailureDrawable(U.app().resources.getDrawable(com.component.busilib.R.drawable.load_img_error))
                                .setLoadingDrawable(U.app().resources.getDrawable(com.component.busilib.R.drawable.loading_place_holder_img))
                                .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_320.w).build())
                                .setBorderColor(Color.parseColor("#3B4E79")).build())
            }
        }
    }

    internal fun execUploadPhoto(photoModel: PhotoModel, callback: UploadCallback?) {
        MyLog.d(TAG, "execUploadPhoto photoModel=$photoModel")
        val uploadTask = UploadParams.newBuilder(photoModel.localPath)
                .setNeedCompress(true)
                .setNeedMonitor(true)
                .setFileType(UploadParams.FileType.audit)
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

    private fun createClub(photoModel: PhotoModel) {
        launch {
            val map = mutableMapOf("logo" to photoModel.picPath, "name" to clubNameEt.text.toString(), "desc" to clubIntroductionEt.text.toString())
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("createClub", ControlType.CancelThis)) {
                clubServerApi.createClub(body)
            }

            if (result.errno == 0) {
                U.getToastUtil().showShort("创建成功")
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
}