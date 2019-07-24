package com.component.dialog

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.component.busilib.R
import kotlinx.android.synthetic.main.notify_dialog_view.view.*

open class NotifyDialogView(
        context: Context,
        attrs: AttributeSet? = null
) : RelativeLayout(context, attrs) {

    var mTitle = ""
    var mContent = ""

    constructor(context: Context, title: String, content: String) : this(context) {
        this.mTitle = title;
        this.mContent = content;

        title_stv.setText(mTitle)
        content_tv.setText(Html.fromHtml(mContent))
    }

    init {
        View.inflate(getContext(), R.layout.notify_dialog_view, this)
    }

}