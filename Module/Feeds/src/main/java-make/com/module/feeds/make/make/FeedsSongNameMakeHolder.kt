package com.module.feeds.make.make

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.common.log.MyLog
import com.module.feeds.R


class FeedsSongNameMakeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var songNameChangeListener: ((String)->Unit)? = null
    val labelTv: TextView
    val editEt: EditText

    var postion: Int? = null
    var songName: String? = null

    init {
        labelTv = itemView.findViewById(R.id.label_tv)
        editEt = itemView.findViewById(R.id.edit_et)

        editEt.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            MyLog.d("FeedsLyricMakeHolder", "postion=$postion item=$songName hasFocus=$hasFocus")
            if (hasFocus) {
                editEt.setBackgroundResource(R.drawable.feeds_lyric_make_editor_bg_focus)
            } else {
                if (TextUtils.isEmpty(songName)) {
                    editEt.setBackgroundResource(R.drawable.feeds_lyric_make_editor_bg_focus)
                } else {
                    editEt.setBackgroundResource(R.drawable.feeds_lyric_make_editor_bg)
                }
            }
        }
        editEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                MyLog.d("FeedsLyricMakeHolder", "")
                songNameChangeListener?.invoke(s.toString())
                songName = s.toString()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
    }

    fun bindData(pos: Int, item: String) {
        this.postion = pos
        this.songName = item
        editEt.setText(item)
    }
}