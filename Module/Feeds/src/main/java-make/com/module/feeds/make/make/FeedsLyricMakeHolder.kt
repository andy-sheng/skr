package com.module.feeds.make.make

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.module.feeds.R
import android.text.*
import android.text.style.ForegroundColorSpan


class FeedsLyricMakeHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val labelTv: TextView
    val originTv: TextView
    val editEt: EditText
    val wordNumTv: TextView

    var postion: Int? = null
    var item: LyricItem? = null

    init {
        labelTv = itemView.findViewById(R.id.label_tv)
        originTv = itemView.findViewById(R.id.origin_tv)
        editEt = itemView.findViewById(R.id.edit_et)
        wordNumTv = itemView.findViewById(R.id.word_num_tv)

        editEt.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            MyLog.d("FeedsLyricMakeHolder", "postion=$postion item=$item hasFocus=$hasFocus")
            if(hasFocus){
                editEt.setBackgroundResource(R.drawable.feeds_lyric_make_editor_bg_focus)
            }else{
                editEt.setBackgroundResource(R.drawable.feeds_lyric_make_editor_bg)
                item?.let {
                    setEtText(it)
                }
            }
        }
        editEt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                MyLog.d("FeedsLyricMakeHolder", "")
                item?.newContent = s?.toString() ?: ""
                wordNumTv.text = "${item?.newContent?.length}/${item?.content?.length}"
//                val textSpan = s
//                if (item?.newContent?.length == item?.content?.length) {
//                    for (i in 0 until (item?.newContent?.length ?: 0)) {
//                        val c = item?.newContent?.get(i)
//                        if (c == item?.content?.get(i)) {
//                            textSpan?.setSpan(ForegroundColorSpan(U.getColor(R.color.black_trans_80)), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                        } else {
//                            textSpan?.setSpan(ForegroundColorSpan(Color.parseColor("#FF8F00")), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                        }
//                    }
//                } else {
//                    textSpan?.setSpan(ForegroundColorSpan(U.getColor(R.color.red)), 0, textSpan?.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//                }
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
            labelTv.text = "歌名:"
            originTv.visibility = View.GONE
        } else if (pos == 1) {
            labelTv.visibility = View.VISIBLE
            labelTv.text = "歌词:"
            originTv.visibility = View.VISIBLE
        } else {
            labelTv.visibility = View.GONE
            originTv.visibility = View.VISIBLE
        }
        originTv.text = item.content
        editEt.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(item.content.length))
        wordNumTv.text = "${item.newContent.length}/${item.content.length}"
        setEtText(item)
    }

    private fun setEtText(item: LyricItem) {
        if (item.newContent.length == item.content.length) {
            val sp = SpanUtils()
            for (i in 0 until item.newContent.length) {
                if (item.newContent[i].equals(item.content[i])) {
                    sp.append(item.newContent[i].toString()).setForegroundColor(U.getColor(R.color.black_trans_80))
                } else {
                    sp.append(item.newContent[i].toString()).setForegroundColor(Color.parseColor("#FF8F00"))
                }
            }
            editEt.setText(sp.create())
        } else {
            val spanBuilder = SpanUtils()
                    .append(item.newContent).setForegroundColor(U.getColor(R.color.red))
                    .create()
            editEt.setText(spanBuilder)
        }
    }
}