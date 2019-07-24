package com.component.person.photo.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.common.log.MyLog
import com.component.busilib.R
import com.component.person.photo.holder.EmptyPhotoHolder
import com.component.person.photo.holder.PhotoAddHolder
import com.component.person.photo.holder.PhotoViewHolder
import com.component.person.photo.model.PhotoModel

import java.util.ArrayList

class PhotoAdapter(internal var mType: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val TAG = "PhotoAdapter"

    internal var mDataList: ArrayList<PhotoModel>? = ArrayList()
    private var mHasUpdate: Boolean = false

    private val PHOTO_ADD_TYPE = 0     //上传
    private val PHOTO_ITEM_TYPE = 1    //照片
    private val PHOTO_ITEM_EMPTY = 2   //空页面

    var mOnClickPhotoListener: ((view: View, position: Int, model: PhotoModel?) -> Unit)? = null
    var mOnClickAddPhotoListener: (() -> Unit)? = null
    var mDeleteListener: ((model: PhotoModel?) -> Unit)? = null
    var mReUploadListener: ((model: PhotoModel?) -> Unit)? = null

    interface PhotoManageListener {
        fun delete(model: PhotoModel)

        fun reupload(model: PhotoModel)
    }

    private val isContainEmpty: Boolean
        get() = if (mType == TYPE_OTHER_PERSON_CENTER) {
            !(mDataList != null && mDataList!!.size > 0)
        } else false

    // 把未成功的挑选出来
    var dataList: ArrayList<PhotoModel>?
        get() = mDataList
        set(dataList) {
            if (dataList != null) {
                val unSuccessList = ArrayList<PhotoModel>()
                for (photoModel in mDataList!!) {
                    if (photoModel.status != PhotoModel.STATUS_SUCCESS) {
                        unSuccessList.add(photoModel)
                    }
                }
                mDataList!!.clear()
                mDataList!!.addAll(unSuccessList)
                mDataList!!.addAll(dataList)
                notifyDataSetChanged()
            }
        }

    /**
     * 列表中上传成功的item个数
     *
     * @return
     */
    val successNum: Int
        get() {
            var success = 0
            for (photoModel in mDataList!!) {
                if (photoModel.status == PhotoModel.STATUS_SUCCESS) {
                    success++
                }
            }
            return success
        }

    init {
        mHasUpdate = mType == TYPE_PERSON_CENTER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            PHOTO_ADD_TYPE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.photo_add_view_layout, parent, false)
                return PhotoAddHolder(view, mOnClickAddPhotoListener)
            }
            PHOTO_ITEM_EMPTY -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.photo_empty_layout, parent, false)
                return EmptyPhotoHolder(view)
            }
            else -> {
                val view: View = if (mType == TYPE_PERSON_CARD) {
                    LayoutInflater.from(parent.context).inflate(R.layout.photo_card_item_view_layout, parent, false)
                } else {
                    LayoutInflater.from(parent.context).inflate(R.layout.photo_item_view_layout, parent, false)
                }
                return PhotoViewHolder(view, mOnClickPhotoListener, mDeleteListener, mReUploadListener)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (isContainEmpty) {
            return
        }

        if (mHasUpdate) {
            if (position != 0) {
                val photoModel = mDataList!![position - 1]
                (holder as PhotoViewHolder).bindData(photoModel, position)
            }
        } else {
            (holder as PhotoViewHolder).bindData(mDataList!![position], position)
        }
    }


    override fun getItemViewType(position: Int): Int {
        if (isContainEmpty) {
            return PHOTO_ITEM_EMPTY
        }

        return if (position == 0 && mHasUpdate) {
            PHOTO_ADD_TYPE
        } else PHOTO_ITEM_TYPE

    }

    override fun getItemCount(): Int {
        if (mHasUpdate) {
            return mDataList!!.size + 1
        }

        return if (isContainEmpty) {
            // 空页面
            1
        } else mDataList!!.size

    }

    /**
     * 头部插入某条数据
     *
     * @param data
     */
    fun insertFirst(data: PhotoModel) {
        mDataList!!.add(0, data)
        if (mHasUpdate) {
            notifyItemInserted(1)
        } else {
            notifyItemInserted(0)
        }
    }

    /**
     * 尾部插入一堆数据
     *
     * @param list
     */
    fun insertLast(list: List<PhotoModel>) {
        val origin = mDataList!!.size
        mDataList!!.addAll(list)
        if (mHasUpdate) {
            notifyItemRangeInserted(origin + 1, mDataList!!.size - origin + 1)
        } else {
            notifyItemRangeInserted(origin, mDataList!!.size - origin)
        }
    }

    fun delete(photoModel: PhotoModel) {
        for (i in mDataList!!.indices) {
            val m = mDataList!![i]
            if (m == photoModel) {
                MyLog.d(TAG, "delete find i=$i")
                mDataList!!.removeAt(i)
                if (mHasUpdate) {
                    notifyItemRemoved(i + 1)
                } else {
                    notifyItemRemoved(i)
                }
                return
            }
        }
    }

    fun update(photoModel: PhotoModel) {
        MyLog.d(TAG, "update photoModel=$photoModel")
        for (i in mDataList!!.indices) {
            val m = mDataList!![i]
            if (m == photoModel) {
                MyLog.d(TAG, "update find i=$i")
                m.status = photoModel.status
                if (mHasUpdate) {
                    notifyItemChanged(i + 1)
                } else {
                    notifyItemChanged(i)
                }
                return
            }
        }
    }

    fun getPostionOfItem(model: PhotoModel?): Int {
        for (i in mDataList!!.indices) {
            val m = mDataList!![i]
            if (m == model) {
                return i
            }
        }
        return 0
    }


    companion object {

        val TYPE_PERSON_CARD = 1 // dialogcarnd
        val TYPE_PERSON_CENTER = 2 // 个人中心
        val TYPE_OTHER_PERSON_CENTER = 3//他人个人中心
    }
}
