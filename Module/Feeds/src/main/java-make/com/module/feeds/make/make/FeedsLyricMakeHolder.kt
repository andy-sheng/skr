package com.module.feeds.make.make

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.common.log.MyLog
import com.module.feeds.R


class FeedsLyricMakeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val labelTv: TextView
    val editEt: EditText
    val wordNumTv: TextView
    var postion:Int? = null
    var item:LyricItem? = null
    init {
        labelTv = itemView.findViewById(R.id.label_tv)
        editEt = itemView.findViewById(R.id.edit_et)
        wordNumTv = itemView.findViewById(R.id.word_num_tv)

        editEt.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            MyLog.d("FeedsLyricMakeHolder","postion=$postion item=$item hasFocus=$hasFocus")
        }
        editEt.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                item?.newContent = s?.toString()?:""
                wordNumTv.text = "${item?.newContent?.length}/${item?.content?.length}"
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
    }

    fun bindData(pos: Int, item: LyricItem) {
        this.postion = pos
        this.item = item
        if (pos == 0) {
            labelTv.visibility = View.VISIBLE
            labelTv.text = "歌曲名"
        } else if (pos == 1) {
            labelTv.visibility = View.VISIBLE
            labelTv.text = "歌 词"
        } else {
            labelTv.visibility = View.GONE
        }
        editEt.setFilters(arrayOf<InputFilter>(InputFilter.LengthFilter(item.content.length)))
        editEt.setText(item.newContent)
        wordNumTv.text = "${item.newContent.length}/${item.content.length}"
    }
}