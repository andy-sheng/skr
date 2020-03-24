package com.module.club.homepage.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import com.common.core.userinfo.model.ClubMemberInfo
import com.module.club.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

// 家族相册
class ClubPhotoView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : ConstraintLayout(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    var clubMemberInfo: ClubMemberInfo? = null

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    init {
        View.inflate(context, R.layout.club_tab_photo_view_layout, this)

    }

    fun loadData(flag: Boolean, callback: () -> Unit?) {
        callback.invoke()
    }

    fun loadMoreData(callback: () -> Unit?) {

    }

    fun destroy() {
        cancel()
    }
}