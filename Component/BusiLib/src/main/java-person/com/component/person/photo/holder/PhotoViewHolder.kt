package com.component.person.photo.holder

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View

import com.common.image.fresco.FrescoWorker
import com.common.image.model.BaseImage
import com.common.image.model.ImageFactory
import com.common.image.model.oss.OssImgFactory
import com.common.utils.ImageUtils
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ex.ExFrameLayout
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.component.busilib.R
import com.component.person.photo.adapter.PhotoAdapter
import com.facebook.drawee.view.SimpleDraweeView
import com.component.person.photo.model.PhotoModel

/**
 * 这里传入 position 的话 insert delete 会导致postion不准
 */
class PhotoViewHolder(itemView: View,
                      var mOnClickPhotoListener: ((view: View, position: Int, model: PhotoModel?) -> Unit)?,
                      var mDeleteListener: ((model: PhotoModel?) -> Unit)?,
                      var mReUploadListener: ((model: PhotoModel?) -> Unit)?) : RecyclerView.ViewHolder(itemView) {

    val TAG = "PhotoViewHolder"

    private var mPhotoIv: SimpleDraweeView = itemView.findViewById(R.id.photo_iv)
    private var mUploadTipsTv: ExTextView = itemView.findViewById(R.id.upload_tips_tv)
    private var mIvBlackBg: ExImageView = itemView.findViewById(R.id.iv_black_bg)

    private var mTvErrorTips: ExTextView = itemView.findViewById(R.id.tv_error_tips)
    private var mErrorContainer: ExFrameLayout = itemView.findViewById(R.id.error_container)

    internal var position: Int = 0
    internal var mPhotoModel: PhotoModel? = null

    init {

        itemView.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                mOnClickPhotoListener?.invoke(v, position, mPhotoModel)
            }
        })

        mIvBlackBg.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                when {
                    mPhotoModel?.status == PhotoModel.STATUS_FAILED -> mReUploadListener?.invoke(mPhotoModel)
                    mPhotoModel?.status == PhotoModel.STATUS_FAILED_SEXY -> mDeleteListener?.invoke(mPhotoModel)
                    mPhotoModel?.status == PhotoModel.STATUS_FAILED_LIMIT -> mDeleteListener?.invoke(mPhotoModel)
                }
            }
        })
    }

    fun bindData(photoModel: PhotoModel, position: Int) {
        this.mPhotoModel = photoModel
        this.position = position

        var path = photoModel.picPath
        if (TextUtils.isEmpty(path)) {
            path = photoModel.localPath
        }

        FrescoWorker.loadImage(mPhotoIv,
                ImageFactory.newPathImage(path)
                        .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                        .setBorderWidth(U.getDisplayUtils().dip2px(2f).toFloat())
                        .setFailureDrawable(U.app().resources.getDrawable(R.drawable.load_img_error))
                        .setLoadingDrawable(U.app().resources.getDrawable(R.drawable.loading_place_holder_img))
                        .setLowImageUri(ImageUtils.SIZE.SIZE_160)
                        .addOssProcessors(OssImgFactory.newResizeBuilder().setW(ImageUtils.SIZE.SIZE_320.w).build())
                        .setBorderColor(Color.parseColor("#3B4E79")).build<BaseImage>())
        mUploadTipsTv.visibility = View.VISIBLE
        mUploadTipsTv.setTextColor(Color.WHITE)
        mErrorContainer.visibility = View.GONE

        when {
            photoModel.status == PhotoModel.STATUS_DELETE -> mUploadTipsTv.text = "删除"
            photoModel.status == PhotoModel.STATUS_UPLOADING -> mUploadTipsTv.text = "正在上传"
            photoModel.status == PhotoModel.STATUS_WAIT_UPLOAD -> mUploadTipsTv.text = "等待上传"
            photoModel.status == PhotoModel.STATUS_FAILED -> {
                mUploadTipsTv.visibility = View.GONE
                mErrorContainer.visibility = View.VISIBLE
                mIvBlackBg.setImageDrawable(U.getDrawable(R.drawable.photo_chonglai))
                mTvErrorTips.text = "上传失败"
            }
            photoModel.status == PhotoModel.STATUS_SUCCESS -> mUploadTipsTv.visibility = View.GONE
            photoModel.status == PhotoModel.STATUS_FAILED_SEXY -> {
                mUploadTipsTv.visibility = View.GONE
                mErrorContainer.visibility = View.VISIBLE
                mIvBlackBg.setImageDrawable(U.getDrawable(R.drawable.photo_shanchu))
                mTvErrorTips.text = "图片敏感"
            }
            photoModel.status == PhotoModel.STATUS_FAILED_LIMIT -> {
                mUploadTipsTv.visibility = View.GONE
                mErrorContainer.visibility = View.VISIBLE
                mIvBlackBg.setImageDrawable(U.getDrawable(R.drawable.photo_shanchu))
                mTvErrorTips.text = "超过上限"
            }
        }
    }
}
