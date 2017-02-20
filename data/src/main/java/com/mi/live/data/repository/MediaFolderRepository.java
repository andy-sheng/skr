package com.mi.live.data.repository;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.mi.live.data.repository.DataType.PhotoFolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

/**
 * 得到媒体库的所有文件夹的repository
 * Created by yaojian on 16-8-22.
 */
public class MediaFolderRepository {


    @Inject
    public MediaFolderRepository(){

    }

    public Observable<List<PhotoFolder>> getAllFolderList(final ContentResolver contentResolver) {

        return Observable.create(new Observable.OnSubscribe<List<PhotoFolder>>() {
            @Override
            public void call(Subscriber<? super List<PhotoFolder>> subscriber) {
                Cursor photoFolderCursor = getAllPhotoFolderCursor(contentResolver);
                Cursor videoFolderCursor = getAllVideoFolderCursor(contentResolver);

                List<PhotoFolder> folderList = new ArrayList<>();
                Map<String, PhotoFolder> folderMap = new HashMap<>();

                int photoIdIndex = photoFolderCursor != null ? photoFolderCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID) : 0;
                int photoNameIndex = photoFolderCursor != null ? photoFolderCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME) : 0;
                int photoPathIndex = photoFolderCursor != null ? photoFolderCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA) : 0;
                int photoTSIndex = photoFolderCursor != null ? photoFolderCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN) : 0;

                int videoIdIndex = videoFolderCursor != null ? videoFolderCursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_ID) : 0;
                int videoNameIndex = videoFolderCursor != null ? videoFolderCursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME) : 0;
                int videoPathIndex = videoFolderCursor != null ? videoFolderCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA) : 0;
                int videoTSIndex = videoFolderCursor != null ? videoFolderCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN) : 0;

                long photoMaxTs = -1, videoMaxTs = -1;
                if (photoFolderCursor != null && photoFolderCursor.moveToNext()) {
                    photoMaxTs = photoFolderCursor.getLong(photoTSIndex);
                }
                if (videoFolderCursor != null && videoFolderCursor.moveToNext()) {
                    videoMaxTs = videoFolderCursor.getLong(videoTSIndex);
                }

                while ((photoMaxTs > 0 || videoMaxTs > 0) && !subscriber.isUnsubscribed()) {
                    PhotoFolder photoFolder = new PhotoFolder();
                    if (photoMaxTs >= videoMaxTs) {
                        photoFolder.setFolderID(photoFolderCursor.getString(photoIdIndex));
                        photoFolder.setFolderName(photoFolderCursor.getString(photoNameIndex));
                        photoFolder.setPhotoPath(photoFolderCursor.getString(photoPathIndex));
                        photoFolder.setPhotoCnt(photoFolderCursor.getInt(0));
                        if (photoFolderCursor.moveToNext()) {
                            photoMaxTs = photoFolderCursor.getLong(photoTSIndex);
                        } else {
                            photoMaxTs = -1;
                        }
                    } else {
                        photoFolder.setFolderID(videoFolderCursor.getString(videoIdIndex));
                        photoFolder.setFolderName(videoFolderCursor.getString(videoNameIndex));
                        photoFolder.setPhotoPath(videoFolderCursor.getString(videoPathIndex));
                        photoFolder.setPhotoCnt(videoFolderCursor.getInt(0));
                        if (videoFolderCursor.moveToNext()) {
                            videoMaxTs = videoFolderCursor.getLong(videoTSIndex);
                        } else {
                            videoMaxTs = -1;
                        }
                    }

                    if (photoFolder.getPhotoPath() != null && (Environment.getExternalStorageDirectory().toString()).equals(photoFolder.getPhotoPath().substring(0, photoFolder.getPhotoPath().lastIndexOf("/")))) {
                        photoFolder.setFolderName("sdcard");
                    }

                    if (folderMap.containsKey(photoFolder.getFolderID())) {
                        PhotoFolder folder = folderMap.get(photoFolder.getFolderID());
                        if (folder != null) {
                            folder.setPhotoCnt(folder.getPhotoCnt() + photoFolder.getPhotoCnt());
                        }
                    } else {
                        folderMap.put(photoFolder.getFolderID(), photoFolder);
                        folderList.add(photoFolder);
                    }
                }

                if (photoFolderCursor != null) {
                    photoFolderCursor.close();
                }
                if (videoFolderCursor != null) {
                    videoFolderCursor.close();
                }

                if(!subscriber.isUnsubscribed()){
                    subscriber.onNext(folderList);
                }
                subscriber.onCompleted();
            }
        });
    }

    //得到图片文件夹的Cursor
    private Cursor getAllPhotoFolderCursor(ContentResolver contentResolver) {
        if (contentResolver == null) {
            return null;
        }
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {"count(" + MediaStore.Images.Media._ID + ")", MediaStore.Images.Media._ID, MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATE_TAKEN};

        String selection = MediaStore.Images.Media.MIME_TYPE + " =? or " + MediaStore.Images.Media.MIME_TYPE + " =?) GROUP BY (" + MediaStore.Images.Media.BUCKET_ID;
        String[] selectionArgs = new String[]{"image/jpeg", "image/png"};
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";
        return contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    public static final int MIN_RECORD_TIME = 3 * 1000;     //过滤视频
    //得到视频文件夹的Cursor
    private Cursor getAllVideoFolderCursor(ContentResolver contentResolver) {
        if (contentResolver == null) {
            return null;
        }
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {"count(" + MediaStore.Video.Media._ID + ")", MediaStore.Video.Media._ID, MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_ID, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.Video.Media.DATE_TAKEN};

        String selection = MediaStore.Video.Media.DURATION + " >=? AND " + MediaStore.Video.Media.MIME_TYPE + " =?) GROUP BY (" + MediaStore.Video.Media.BUCKET_ID;
        String[] selectionArgs = new String[]{String.valueOf(MIN_RECORD_TIME), "video/mp4"};
        String sortOrder = MediaStore.Video.Media.DATE_TAKEN + " DESC";
        return contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
    }

}
