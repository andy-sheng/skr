package com.module.mall.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.common.core.avatar.AvatarUtils
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.BaseImageView
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.module.home.R
import com.module.mall.event.BuyMallEvent
import com.module.mall.event.GiveMallEvent
import com.module.mall.model.ProductModel
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import org.greenrobot.eventbus.EventBus

class BuyEffectDialogView : ConstraintLayout {
    internal var mDialogPlus: DialogPlus? = null

    var bgIv: ImageView
    var effectIv: BaseImageView
    var effectNameTv: ExTextView
    var firstDes: ExTextView
    var secondDes: ExTextView
    var buyTv: ExTextView
    var buyGive: ExTextView
    var model: ProductModel? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        View.inflate(context, R.layout.buy_effect_dialog_view_layout, this)

        bgIv = rootView.findViewById(R.id.bg_iv)
        effectIv = rootView.findViewById(R.id.effect_iv)
        effectNameTv = rootView.findViewById(R.id.effect_name_tv)
        firstDes = rootView.findViewById(R.id.first_des)
        secondDes = rootView.findViewById(R.id.second_des)
        buyTv = rootView.findViewById(R.id.buy_tv)
        buyGive = rootView.findViewById(R.id.buy_give)

        firstDes.setDebounceViewClickListener {
            firstDes.isSelected = true
            secondDes.isSelected = false
        }

        secondDes.setDebounceViewClickListener {
            firstDes.isSelected = false
            secondDes.isSelected = true
        }

        buyTv.setDebounceViewClickListener {
            dismiss(false)
            EventBus.getDefault().post(BuyMallEvent(model!!, if (firstDes.isSelected) model!!.price[0] else model!!.price[1]))
        }

        buyGive.setDebounceViewClickListener {
            dismiss(false)
            EventBus.getDefault().post(GiveMallEvent(model!!, if (firstDes.isSelected) model!!.price[0] else model!!.price[1]))
        }
    }

    @JvmOverloads
    fun showByDialog(canCancel: Boolean, model: ProductModel) {
        if (mDialogPlus != null) {
            mDialogPlus!!.dismiss(false)
        }

        this.model = model
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setCancelable(canCancel)
                .create()
        mDialogPlus!!.show()

        firstDes.isSelected = true
        secondDes.isSelected = false

        AvatarUtils.loadAvatarByUrl(effectIv, AvatarUtils.newParamsBuilder(model.goodsURL)
                .setCornerRadius(U.getDisplayUtils().dip2px(8f).toFloat())
                .build())

        effectNameTv.text = model.goodsName

        firstDes.visibility = View.GONE
        secondDes.visibility = View.GONE

        repeat(if (model.price.size > 2) 2 else model.price.size) {
            if (it == 0) {
                firstDes.text = "X${model.price[it].realPrice} /${model.price[it].buyTypeDes}"
                firstDes.visibility = View.VISIBLE
            } else if (it == 1) {
                secondDes.text = "X${model.price[it].realPrice} /${model.price[it].buyTypeDes}"
                secondDes.visibility = View.VISIBLE
            }
        }
    }

    fun dismiss() {
        if (mDialogPlus != null) {
            mDialogPlus!!.dismiss()
        }
    }

    fun dismiss(isAnimation: Boolean) {
        if (mDialogPlus != null) {
            mDialogPlus!!.dismiss(isAnimation)
        }
    }
}