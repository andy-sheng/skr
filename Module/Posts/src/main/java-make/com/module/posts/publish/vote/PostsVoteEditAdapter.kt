package com.module.posts.publish.vote

import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.common.image.fresco.BaseImageView
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.utils.dp
import com.common.view.ex.NoLeakEditText
import com.module.posts.R
import com.respicker.model.ImageItem

class PostsVoteEditAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val dataList = ArrayList<EditContentModel>()

    var delClickListener: ((model: EditContentModel?, pos: Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.posts_publish_item_vote_edit_layout, parent, false)
        return VoteEditViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is VoteEditViewHolder) {
            holder.bindData(dataList[position], position)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class VoteEditViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var voteEt: NoLeakEditText = view.findViewById(R.id.vote_et)
        var voteDelIv: ImageView = view.findViewById(R.id.vote_del_iv)
        var model: EditContentModel? = null
        var pos: Int = 0

        init {
            voteDelIv.setOnClickListener {
                delClickListener?.invoke(model, pos)
            }
            voteEt.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    model?.content = s.toString()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

            })
        }

        fun bindData(model: EditContentModel, pos: Int) {
            this.model = model
            this.pos = pos
            voteEt.setText(model.content)
            if (pos >= 2) {
                voteDelIv.visibility = View.VISIBLE
            } else {
                voteDelIv.visibility = View.GONE
            }

        }
    }
}

class EditContentModel(var content:String) {
}

