package com.module.playways.party.bgmusic

import android.provider.MediaStore
import com.common.log.MyLog
import com.common.utils.U
import java.io.Serializable

class MusicInfo : Serializable {
    var id: Long = 0
    var title: String? = null
    var path: String? = null
    var album: String? = null
    var artist: String? = null
    var duration: Int = 0
    var size: Long = 0
    var dateAdd: String? = null
    var mineType: String? = null

    override fun toString(): String {
        return "MusicInfo(id=$id, title=$title, path=$path, album=$album, artist=$artist, duration=$duration, size=$size, dateAdd=$dateAdd, mineType=$mineType)"
    }

}

//class BgMusicManager{

val MUSIC_PROJECTION = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DATA, // 路径
        MediaStore.Audio.Media.ALBUM,//封面图？
        MediaStore.Audio.Media.ARTIST,// 艺术家
        MediaStore.Audio.Media.DURATION,// 总时间
        MediaStore.Audio.Media.SIZE,
        MediaStore.Audio.Media.DATE_ADDED,
        MediaStore.Audio.Media.MIME_TYPE
)

public fun getLocalMusicInfo(): List<MusicInfo> {
    var results = ArrayList<MusicInfo>()
    val resolver = U.app().contentResolver
    //TODO 先不加过滤

    var selections = StringBuilder()
//            .append(MediaStore.Audio.Media.MIME_TYPE)
//            .append(" in (")
//            .append("'audio/mpeg'").append(",")
//            .append("'audio/mp3'")
//            .append(")")
//            .append(" and ")
            .append(MediaStore.Audio.Media.DURATION)
            .append("> " + 60 * 1000 * 2)
            .append(" and ")
            .append(MediaStore.Audio.Media.ARTIST)
            .append("!='<unknown>'")
            .toString()
    var selectionArgs = arrayOf("<unknown>")

    val cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MUSIC_PROJECTION,
            selections,
            null, MediaStore.Audio.Media.DATE_ADDED
    )
    if (cursor != null) {
        val indexID = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
        val indexTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
        val indexData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
        val indexAlbum = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
        val indexArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
        val indexDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
        val indexSize = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
        val indexDateAdd = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
        val indexMineType = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
        while (cursor.moveToNext()) {
            var musicInfo = MusicInfo()
            musicInfo.id = cursor.getLong(indexID)
            musicInfo.title = cursor.getString(indexTitle)
            musicInfo.path = cursor.getString(indexData)
            musicInfo.album = cursor.getString(indexAlbum)
            musicInfo.artist = cursor.getString(indexArtist)
            musicInfo.duration = cursor.getInt(indexDuration)
            musicInfo.size = cursor.getLong(indexSize)
            musicInfo.dateAdd = cursor.getString(indexDateAdd)
            musicInfo.mineType = cursor.getString(indexMineType)
            MyLog.d(musicInfo.toString())
            results.add(musicInfo)
        }
        cursor.close()
    }
    return results
}
//}