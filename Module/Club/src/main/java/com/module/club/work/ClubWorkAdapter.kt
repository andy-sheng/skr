package com.module.club.work

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.OrientationEventListener
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.view.setDebounceViewClickListener
import com.common.image.fresco.FrescoWorker
import com.common.image.model.ImageFactory
import com.common.log.MyLog
import com.common.utils.dp
import com.component.busilib.view.AvatarView
import com.facebook.drawee.view.SimpleDraweeView
import com.module.club.R

class ClubWorkAdapter(var workListener: ClubWorkAdapter.WorkListener ) : RecyclerView.Adapter<ClubWorkAdapter.PartyAreaItemHolder>() {

    var mDataList = ArrayList<WorkModel>()
    var clickListener: ((position: Int, model: WorkModel, imagePlay: ImageView) -> Unit)? = null
    var mCurrentPlayModel: WorkModel? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyAreaItemHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_work_view_layout, parent, false)
        return PartyAreaItemHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: PartyAreaItemHolder, position: Int) {
        holder.bindData(position, mDataList[position])
    }

    inner class PartyAreaItemHolder(item: View) : RecyclerView.ViewHolder(item) {
         val avatarIv: AvatarView = item.findViewById(R.id.cover_sdv)
        val nameTextView: TextView = item.findViewById(R.id.name_tv)
        val statusTextView: TextView = item.findViewById(R.id.status_tv)
        val tagTextView: TextView = item.findViewById(R.id.tag_tv)
        var imagePlay: ImageView = item.findViewById(R.id.record_play_iv)

        private lateinit var mModl: WorkModel
        private var mPos: Int = -1

        init {
            item.setDebounceViewClickListener {
                workListener.onClickPostsAvatar(mPos, mModl);
            }
            imagePlay.setDebounceViewClickListener {
                clickListener?.invoke(mPos, mModl, imagePlay)

            }
        }


       private fun  workModel2UserInfoModel(model: WorkModel): UserInfoModel{
            var userInfoModel =  UserInfoModel()
            userInfoModel.avatar = model.avatar
           return userInfoModel
        }


        fun bindData(position: Int, model: WorkModel) {
            this.mModl = model
            this.mPos = position
            //TODO 字段可能要调整
          /*  FrescoWorker.loadImage(coverSdv, ImageFactory.newPathImage(model?.avatar)
                    .setCornerRadius(8.dp().toFloat())
                    .build())*/

            if (model != null) {
                avatarIv.bindData(workModel2UserInfoModel(model))
            } else {
                MyLog.e("PostsWatchViewHolder", "bindData error pos = $, model = $model")
            }



            nameTextView.text = model?.nickName
            statusTextView.text = model?.songName
            tagTextView.text = model?.artist
        }
    }

    interface WorkListener {
        fun onClickPostsAvatar(position: Int, model: WorkModel?)  // 发帖头像
    }
}