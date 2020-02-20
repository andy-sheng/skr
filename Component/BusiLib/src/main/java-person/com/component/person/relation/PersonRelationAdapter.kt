package com.component.person.relation

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.common.core.avatar.AvatarUtils
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.component.busilib.R
import com.component.person.model.RelationInfo
import com.component.person.model.RelationModel
import com.component.person.utils.RelationResUtils
import com.facebook.drawee.view.SimpleDraweeView

class PersonRelationAdapter(val listener: Listener?) : RecyclerView.Adapter<PersonRelationAdapter.RelationViewHolder>() {

    var mDataList = ArrayList<RelationModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.person_relation_item_layout, parent, false)
        return RelationViewHolder(view)
    }

    override fun getItemCount(): Int {
        if (mDataList.size > 5) {
            return 5
        }
        return mDataList.size
    }

    override fun onBindViewHolder(holder: RelationViewHolder, position: Int) {
        holder.bindData(mDataList[position], position)
    }

    inner class RelationViewHolder(item: View) : RecyclerView.ViewHolder(item) {

        private val avatarIv: SimpleDraweeView = item.findViewById(R.id.avatar_iv)
        private val relationTag: TextView = item.findViewById(R.id.relation_tag)

        var mModel: RelationModel? = null
        var mPos = -1

        init {
            avatarIv.setDebounceViewClickListener {
                listener?.onClickItem(mPos, mModel)
            }

            relationTag.setDebounceViewClickListener {
                listener?.onClickItem(mPos, mModel)
            }
        }

        fun bindData(model: RelationModel, position: Int) {
            this.mModel = model
            this.mPos = position

            AvatarUtils.loadAvatarByUrl(avatarIv, AvatarUtils.newParamsBuilder(model.user?.avatar)
                    .setCircle(true)
                    .build())
            // 得先判断道具卡是不是守护
            if(model.relationInfo?.displayType == RelationInfo.GDT_GUARD){
                relationTag.visibility = View.VISIBLE;
                relationTag.text = "守护"
                relationTag.background = U.getDrawable(R.drawable.relation_guard_icon)
            }else{
                if (RelationResUtils.getDrawable(model.relationInfo?.relationType) != null) {
                    relationTag.visibility = View.VISIBLE
                    relationTag.text = RelationResUtils.getDesc(model.relationInfo?.relationType)
                    relationTag.background = RelationResUtils.getDrawable(model.relationInfo?.relationType)
                } else {
                    relationTag.visibility = View.GONE
                }
            }

        }
    }

    interface Listener {
        fun onClickItem(position: Int, model: RelationModel?)
    }
}