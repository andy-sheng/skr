package com.module.playways.party.room.view

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.TextView
import com.alibaba.fastjson.JSON
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.common.view.ExViewStub
import com.facebook.drawee.view.SimpleDraweeView
import com.module.playways.R
import com.module.playways.grab.room.dynamicmsg.DynamicModel
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.model.PartyEmojiInfoModel
import kotlinx.coroutines.launch

class PartyEmojiView(viewStub: ViewStub) : ExViewStub(viewStub) {

    private var recyclerView: RecyclerView? = null
    private val adapter = PartyEmojiAdapter()

    private val roomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)
    var hasLoadData = false

    override fun init(parentView: View) {
        recyclerView = parentView.findViewById(R.id.recycler_view)

        recyclerView?.layoutManager = LinearLayoutManager(parentView.context, LinearLayoutManager.HORIZONTAL, false)
        recyclerView?.adapter = adapter
    }

    fun bindData() {
        if (!hasLoadData) {
            loadEmojiData()
        }
    }

    private fun loadEmojiData() {
        val saveTs = U.getPreferenceUtils().getSettingLong(U.getPreferenceUtils().longlySp(), "pref_party_emojis_save_ts", 0)
        if (System.currentTimeMillis() - saveTs > 3600 * 1000 * 6) {
            syncEmojis()
        } else {
            val listStr = U.getPreferenceUtils().getSettingString(U.getPreferenceUtils().longlySp(), "pref_party_emojis", "")
            val list = JSON.parseArray(listStr, PartyEmojiInfoModel::class.java)
            if (list != null && list.size > 0) {
                showEmojiModels(list)
            } else {
                syncEmojis()
            }
        }
    }

    private fun syncEmojis() {
        launch {
            val result = subscribe(RequestControl("syncEmojis", ControlType.CancelThis)) {
                roomServerApi.getEmojiList()
            }
            if (result.errno == 0) {
                val list = JSON.parseArray(result.data.getString("emojis"), PartyEmojiInfoModel::class.java)
                U.getPreferenceUtils().setSettingString(U.getPreferenceUtils().longlySp(), "pref_party_emojis", result.data.getString("emojis"))
                U.getPreferenceUtils().setSettingLong(U.getPreferenceUtils().longlySp(), "pref_party_emojis_save_ts", System.currentTimeMillis())
                showEmojiModels(list)
            } else {

            }
        }
    }

    private fun showEmojiModels(list: List<PartyEmojiInfoModel>?) {
        adapter.mDataList.clear()
        if (!list.isNullOrEmpty()) {
            hasLoadData = true
            adapter.mDataList.addAll(list)
        }
        adapter.notifyDataSetChanged()
    }

    override fun layoutDesc(): Int {
        return R.layout.party_emoji_view_layout
    }

    class PartyEmojiAdapter : RecyclerView.Adapter<PartyEmojiAdapter.PartyEmojiViewHolder>() {

        var mDataList = ArrayList<PartyEmojiInfoModel>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyEmojiViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.party_emoji_item_layout, parent, false)
            return PartyEmojiViewHolder(view)
        }

        override fun getItemCount(): Int {
            return mDataList.size
        }

        override fun onBindViewHolder(holder: PartyEmojiViewHolder, position: Int) {
            holder.bindData(position, mDataList[position])
        }


        inner class PartyEmojiViewHolder(item: View) : RecyclerView.ViewHolder(item) {

            val emojiIv: SimpleDraweeView = item.findViewById(R.id.emoji_iv)
            val emojiDesc: TextView = item.findViewById(R.id.emoji_desc)

            var mPos = -1
            var mModel: PartyEmojiInfoModel? = null

            init {
                item.setDebounceViewClickListener { }
            }

            fun bindData(position: Int, model: PartyEmojiInfoModel) {
                this.mPos = position
                this.mModel = model

                FrescoWorker.loadImage(emojiIv, ImageFactory.newPathImage(model.smallEmojiURL)
                        .build())
                // todo  缺少描述信息
                emojiDesc.text = "缺少描述信息"
            }
        }

    }
}