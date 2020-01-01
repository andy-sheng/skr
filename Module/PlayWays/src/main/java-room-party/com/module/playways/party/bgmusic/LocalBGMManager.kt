package com.module.playways.party.bgmusic

import android.provider.MediaStore
import com.common.utils.U
import java.io.Serializable

class MusicInfo : Serializable {
    var id: Long = 0
    var title: String? = null
    var data: String? = null
    var album: String? = null
    var artist: String? = null
    var duration: Int = 0
    var size: Long = 0
    override fun toString(): String {
        return "MusicInfo(id=$id, title=$title, data=$data, album=$album, artist=$artist, duration=$duration, size=$size)"
    }

}

//class BgMusicManager{

val MUSIC_PROJECTION = arrayOf(
        MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.ALBUM,
        MediaStore.Audio.Media.ARTIST,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.SIZE
)

public fun getLocalMusicInfo(): List<MusicInfo> {
    var results = ArrayList<MusicInfo>()
    val resolver = U.app().contentResolver
    //TODO 先不加过滤
    val cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MUSIC_PROJECTION,
            null,
            null, MediaStore.Audio.Media.DATA
    )
    if (cursor != null) {
        val indexID = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
        val indexTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
        val indexData = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
        val indexAlbum = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
        val indexArtist = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
        val indexDuration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
        val indexSize = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
        while (cursor.moveToNext()) {
            var musicInfo = MusicInfo()
            musicInfo.id = cursor.getLong(indexID)
            musicInfo.title = cursor.getString(indexTitle)
            musicInfo.data = cursor.getString(indexData)
            musicInfo.album = cursor.getString(indexAlbum)
            musicInfo.artist = cursor.getString(indexArtist)
            musicInfo.duration = cursor.getInt(indexDuration)
            musicInfo.size = cursor.getLong(indexSize)
            results.add(musicInfo)
        }
        cursor.close()
    }
    return results
}
//}