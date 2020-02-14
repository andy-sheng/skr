package com.component.person.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.userinfo.model.ClubInfo
import com.common.utils.dp
import com.common.view.ex.ExTextView
import com.component.busilib.R
import com.component.person.utils.StringFromatUtils
import com.facebook.drawee.view.SimpleDraweeView

// 个人主页的家族
class PersonClubView : ConstraintLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val clubLogoSdv: SimpleDraweeView
    private val clubNameTv: TextView
    private val clubDescTv: ExTextView
    private val clubNumTv: TextView
    private val popularTv: ExTextView

    init {
        View.inflate(context, R.layout.person_center_club_view, this)

        clubLogoSdv = this.findViewById(R.id.club_logo_sdv)
        clubNameTv = this.findViewById(R.id.club_name_tv)
        clubDescTv = this.findViewById(R.id.club_desc_tv)
        clubNumTv = this.findViewById(R.id.club_num_tv)
        popularTv = this.findViewById(R.id.popular_tv)
    }

    fun bindData(model: ClubInfo?) {
        model?.let {
            AvatarUtils.loadAvatarByUrl(clubLogoSdv,
                    AvatarUtils.newParamsBuilder(it.logo)
                            .setCircle(false)
                            .setCornerRadius(8.dp().toFloat())
                            .build())
            clubNameTv.text = it.name.trim()
            clubDescTv.text = it.desc.trim()
            clubNumTv.text = it.memberCnt.toString()
            popularTv.text = StringFromatUtils.formatMillion(it.hot)
        }
    }
}