package com.module.mall.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import com.common.image.fresco.BaseImageView
import com.common.view.ex.ExTextView
import com.module.home.R
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder

class BuyEffectDialogView : ConstraintLayout {
    internal var mDialogPlus: DialogPlus? = null

    var bgIv: ImageView
    var effectIv: BaseImageView
    var effectNameTv: ExTextView
    var firstDes: ExTextView
    var secondDes: ExTextView
    var buyTv: ExTextView

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
    }

    @JvmOverloads
    fun showByDialog(canCancel: Boolean = true) {
        if (mDialogPlus != null) {
            mDialogPlus!!.dismiss(false)
        }
        mDialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(ViewHolder(this))
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setExpanded(false)
                .setCancelable(canCancel)
                .create()
        mDialogPlus!!.show()
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