package com.component.person.photo.model

import com.component.person.photo.manager.PhotoModelDB

import java.io.Serializable

class PhotoModel : Serializable {

    companion object {
        val STATUS_SUCCESS = 0// 上传成功
        val STATUS_WAIT_UPLOAD = 1// 等待上传
        val STATUS_UPLOADING = 2// 上传中
        val STATUS_DELETE = 4// 删除了
        val STATUS_FAILED = 3// 上传失败
        val STATUS_FAILED_SEXY = 5// 上传失败 违规
        val STATUS_FAILED_LIMIT = 6// 上传失败 超出100张限制
        fun toPhotoDB(pm: PhotoModel): PhotoModelDB {
            val photoModelDB = PhotoModelDB()
            photoModelDB.localPath = pm.localPath
            photoModelDB.status = pm.status
            return photoModelDB
        }

        fun fromDB(photoModelDB: PhotoModelDB): PhotoModel {
            val pm = PhotoModel()
            pm.localPath = photoModelDB.localPath
            pm.status = photoModelDB.status
            return pm
        }
    }

    /**
     * picID : 0
     * picPath : string
     */
    var picID: Int = 0
    var picPath: String? = null

    var localPath: String? = null// 本地路径
    var status = STATUS_SUCCESS

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as PhotoModel?
        if (status == STATUS_SUCCESS && that!!.status == STATUS_SUCCESS) {
            // 都上传成功了 ID path 有一个一样就认为同一张是同一张照片
            if (picPath != null && picPath == that.picPath) {
                return true
            }
            if (picID == that.picID) {
                return true
            }
        } else {
            // 只要有一个上传成功，就比本地的
            if (localPath != null && localPath == that!!.localPath) {
                return true
            }
        }
        return false
    }

    override fun hashCode(): Int {
        var result = picID
        result = 31 * result + if (picPath != null) picPath!!.hashCode() else 0
        result = 31 * result + if (localPath != null) localPath!!.hashCode() else 0
        return result
    }

    override fun toString(): String {
        return "PhotoModel{" +
                "picID=" + picID +
                ", picPath='" + picPath + '\''.toString() +
                ", localPath='" + localPath + '\''.toString() +
                ", status='" + status + '\''.toString() +
                '}'.toString()
    }
}
