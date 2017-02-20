package com.mi.live.data.repository;


import android.content.ContentResolver;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.mi.live.data.repository.DataType.MediaItem;
import com.mi.live.data.repository.DataType.VideoItem;
import com.base.log.MyLog;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;

import static com.mi.live.data.repository.MediaFolderRepository.MIN_RECORD_TIME;


/**
 * Created by zhangzhiyuan on 16-6-30.
 *
 * @module 图片选择数据源
 * @Owner xionganping
 */
public class PictureRepository {


    private static final String TAG = PictureRepository.class.getSimpleName();
    private static int VIEW_REFRESH_NUM_INTERVAL = 20;

    private int mVideoThumbDataColumnIndex = -1;
    private int mImageThumbDataColumnIndex = -1;

    private static final int KB = 1024;
    private static final int MB = 1024 * KB;
    private static long MAX_VIDEO_FILE_SIZE = 20 * MB;

    @Inject
    public PictureRepository() {

    }

    public Observable<List<VideoItem>> getAllMp4VideoList(final ContentResolver contentResolver) {
        return Observable.create(new Observable.OnSubscribe<List<VideoItem>>() {
            @Override
            public void call(Subscriber<? super List<VideoItem>> subscriber) {

                Cursor videoCursor = getMp4VideoCursor(contentResolver);
                if (videoCursor == null || !videoCursor.moveToFirst()) {
                    subscriber.onCompleted();
                    if (videoCursor != null) {
                        videoCursor.close();
                    }
                    return;
                }

                int videoDurationIndex = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                int videoDataIndex = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                List<VideoItem> itemList = new ArrayList<>();
                List<VideoItem> addList = new ArrayList<>();
                int progress = 0;
                do {
                    int videoDuration = videoCursor.getInt(videoDurationIndex);
                    MyLog.w(TAG, "videoDuration=" + videoDuration);
                    if (videoDuration < MIN_RECORD_TIME) {
                        continue;
                    }

                    String videoFilePath = videoCursor.getString(videoDataIndex);
                    if(needSkipThisVedio(videoFilePath, new IVedioFileSkipFilter[]{new NotExistVedioFileSkipFilter(), new TypeVedioFileSkipFilter(), new SizeVedioFileSkipFilter()})){
                        continue;
                    }

                    VideoItem feedsItem = new VideoItem();
                    feedsItem.setLocalPath(videoCursor.getString(videoDataIndex));
                    feedsItem.setDuration(videoDuration);

                    if (itemList.size() == 0) {
                        feedsItem.mIsFirstItem = true;
                    }
                    addList.add(feedsItem);
                    itemList.add(feedsItem);
                    if (++progress >= VIEW_REFRESH_NUM_INTERVAL) {
                        if (!subscriber.isUnsubscribed()) {
                            List<VideoItem> insertList = new ArrayList<>(addList);
                            subscriber.onNext(insertList);
                            progress = 0;
                            addList.clear();
                        }
                    }

                } while (videoCursor.moveToNext() && !subscriber.isUnsubscribed());

                if (videoCursor != null) {
                    videoCursor.close();
                }

                if (!subscriber.isUnsubscribed() && itemList.size() > 0) {
                    subscriber.onNext(itemList);
                }

                subscriber.onCompleted();
            }
        });
    }

    /**
     * 从content provider获得所有的图片和视频
     *
     * @param contentResolver
     * @param selection
     * @param selectionArgs
     * @param videoSelection
     * @param videoSelectionArgs
     * @return
     */
    public Observable<List<MediaItem>> getAllFeedsList(final ContentResolver contentResolver, final String selection, final String[] selectionArgs, final String videoSelection, final String[] videoSelectionArgs) {

        return Observable.create(new Observable.OnSubscribe<List<MediaItem>>() {
            @Override
            public void call(Subscriber<? super List<MediaItem>> subscriber) {

                Cursor photoCursor = getPhotoCursor(contentResolver, selection, selectionArgs);
                Cursor videoCursor = getVideoCursor(contentResolver, videoSelection, videoSelectionArgs);

                if (photoCursor == null && videoCursor == null) {
                    subscriber.onCompleted();
                    return;
                }

                List<MediaItem> itemList = new ArrayList<>();
                List<MediaItem> addList = new ArrayList<>();
                int photoDataIndex = photoCursor != null ? photoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA) : 0;
                int photoTSIndex = photoCursor != null ? photoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN) : 0;
                int photoIdIndex = photoCursor != null ? photoCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID) : 0;

                int videoDataIndex = videoCursor != null ? videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA) : 0;
                int videoTSIndex = videoCursor != null ? videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN) : 0;
                int videoIDIndex = videoCursor != null ? videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID) : 0;
                int videoDurationIndex = videoCursor != null ? videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION) : 0;


                long photoMaxTs = -1, videoMaxTs = -1;
                if (photoCursor != null && photoCursor.moveToNext()) {
                    photoMaxTs = photoCursor.getLong(photoTSIndex);
                }
                if (videoCursor != null && videoCursor.moveToNext()) {
                    videoMaxTs = videoCursor.getLong(videoTSIndex);
                }
                int progress = 0;
                int progressSum = 0;
                while ((photoMaxTs > 0 || videoMaxTs > 0)) {
                    MyLog.v(TAG + " getAllFeedsList progress == " + progress);
                    MediaItem feedsItem = new MediaItem();
                    if (photoMaxTs >= videoMaxTs) {     //图片
                        if (photoCursor == null) {
                            MyLog.w(TAG + " getAllFeedsList photoCursor == null");
                            break;
                        } else {
                            feedsItem.mPhotoPath = photoCursor.getString(photoDataIndex);
                            if (TextUtils.isEmpty(feedsItem.mPhotoPath)) {
                                if (photoCursor.moveToNext()) {
                                    photoMaxTs = photoCursor.getLong(photoTSIndex);
                                } else {
                                    photoMaxTs = -1;
                                }
                                continue;
                            }
                            //取系统图片缩略图导致部分图片显示不出来了，先注释掉
                            //int id = photoCursor.getInt(photoIdIndex);
                            //feedsItem.mThumbPath  = getImageThumbPath(contentResolver,id);
                            try {
                                File file = new File(feedsItem.mPhotoPath);
                                if (!file.exists()) {
                                    if (photoCursor.moveToNext()) {
                                        photoMaxTs = photoCursor.getLong(photoTSIndex);
                                    } else {
                                        photoMaxTs = -1;
                                    }
                                    continue;
                                }
                            } catch (Exception e) {
                                MyLog.e(e);
                                if (photoCursor.moveToNext()) {
                                    photoMaxTs = photoCursor.getLong(photoTSIndex);
                                } else {
                                    photoMaxTs = -1;
                                }
                                continue;
                            }

                            //产品要求 只有cms后台才能发gif 根据后缀过滤掉gif的图
                            int indexOfDot = feedsItem.mPhotoPath.lastIndexOf('.');
                            if (indexOfDot > 0 && indexOfDot < feedsItem.mPhotoPath.length()) {
                                String extension = feedsItem.mPhotoPath.substring(indexOfDot + 1);
                                MyLog.v(TAG + " getAllFeedsList extension == " + extension);
                                if (!TextUtils.isEmpty(extension) && extension.toLowerCase().equals("gif")) {
                                    if (photoCursor.moveToNext()) {
                                        photoMaxTs = photoCursor.getLong(photoTSIndex);
                                    } else {
                                        photoMaxTs = -1;
                                    }
                                    continue;
                                }
                            }

                            feedsItem.mThumbPath = feedsItem.mPhotoPath;
                            feedsItem.mType = MediaItem.TYPE_PICTURE;
                            if (photoCursor.moveToNext()) {
                                photoMaxTs = photoCursor.getLong(photoTSIndex);
                            } else {
                                photoMaxTs = -1;
                            }
                        }
                    } else {    //视频
                        if (videoCursor == null) {
                            MyLog.w(TAG + " getAllFeedsList videoCursor == null");
                            break;
                        } else {
                            String videoFilePath = videoCursor.getString(videoDataIndex);
                            boolean needSkipThisFile = needSkipThisVedio(videoFilePath, new IVedioFileSkipFilter[]{new NotExistVedioFileSkipFilter(), new TypeVedioFileSkipFilter(), new SizeVedioFileSkipFilter()});
                            if(needSkipThisFile){
                                if (videoCursor.moveToNext()) {
                                    videoMaxTs = videoCursor.getLong(videoTSIndex);
                                } else {
                                    videoMaxTs = -1;
                                }
                                continue;
                            }else{
                                feedsItem.mVideoPath = videoFilePath;

                            }


                            feedsItem.mVideoPath = videoCursor.getString(videoDataIndex);
                            MyLog.v(TAG + " getAllFeedsList feedsItem.mVideoPath == " + feedsItem.mVideoPath);

                            int id = videoCursor.getInt(videoIDIndex);
                            feedsItem.mDuration = videoCursor.getInt(videoDurationIndex);
                            feedsItem.mType = MediaItem.TYPE_VIDEO;
                            feedsItem.mThumbPath = getVideoThumbPath(contentResolver, id);
                            if (TextUtils.isEmpty(feedsItem.mThumbPath)) {
                                feedsItem.mThumbPath = createVideoThumbPath(feedsItem.mVideoPath);
                            }
                            if (TextUtils.isEmpty(feedsItem.mThumbPath)) {
                                if (videoCursor.moveToNext()) {
                                    videoMaxTs = videoCursor.getLong(videoTSIndex);
                                } else {
                                    videoMaxTs = -1;
                                }
                                continue;
                            }

                            feedsItem.mPhotoPath = feedsItem.mThumbPath;
                            if (videoCursor.moveToNext()) {
                                videoMaxTs = videoCursor.getLong(videoTSIndex);
                            } else {
                                videoMaxTs = -1;
                            }
                        }
                    }

                    if (itemList.size() == 0) {
                        feedsItem.mIsFirstItem = true;
                    }

                    addList.add(feedsItem);
                    itemList.add(feedsItem);
                    if (++progress >= VIEW_REFRESH_NUM_INTERVAL) {
                        List<MediaItem> insertList = new ArrayList<>(addList);
                        subscriber.onNext(insertList);
                        progressSum += VIEW_REFRESH_NUM_INTERVAL;
                        //Log.d(TAG,"progressSum = "+progressSum);
                        progress = 0;
                        addList.clear();
                    }
                }

                if (photoCursor != null) {
                    photoCursor.close();
                }
                if (videoCursor != null) {
                    videoCursor.close();
                }

                if (addList.size() > 0) {
                    subscriber.onNext(addList);
                    progressSum += addList.size();
                }
                //Log.d(TAG,"progressSum = "+progressSum);
                subscriber.onCompleted();
            }
        });
    }

    //得到图片文件的Cursor
    public Cursor getPhotoCursor(ContentResolver contentResolver, String selection, String[] selectionArgs) {

        if (contentResolver == null) {
            return null;
        }
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.MINI_THUMB_MAGIC,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN};
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";
        return contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    //得到视频文件的Cursor
    public Cursor getVideoCursor(ContentResolver contentResolver, String selection, String[] selectionArgs) {

        if (contentResolver == null) {
            return null;
        }
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media.MINI_THUMB_MAGIC,
                MediaStore.Video.Media.BUCKET_ID,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_TAKEN};
        String sortOrder = MediaStore.Video.Media.DATE_TAKEN + " DESC";
        return contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    private Cursor getMp4VideoCursor(ContentResolver contentResolver) {
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.MediaColumns.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.MIME_TYPE};
        String sortOrder = MediaStore.Video.Media.DATE_TAKEN + " DESC";

        String selection = MediaStore.Video.Media.MIME_TYPE + "=?";
        String[] selectionArgs = {"video/mp4"};

        Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);
        return cursor;
    }


    public String getVideoThumbPath(ContentResolver contentResolver, int videoId) {
        String path = null;
        if (contentResolver == null) {
            return path;
        }
        Cursor thumbCursor = getVideoThumbCursor(contentResolver, videoId);
        if (thumbCursor != null && thumbCursor.moveToFirst()) {
            if (mVideoThumbDataColumnIndex < 0) {
                mVideoThumbDataColumnIndex = thumbCursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA);
            }
            path = thumbCursor.getString(mVideoThumbDataColumnIndex);
            //Log.d("PicturePresenter","getVideoThumbPath item.mThumbPath　isNotEmpty　videoId=  "+videoId+"　path=  "+path);
        } else {
            //Log.d("PicturePresenter","getVideoThumbPath item.mThumbPath　isEmpty　videoId=  "+videoId);
        }
        if (thumbCursor != null) {
            thumbCursor.close();
        }
        return path;
    }

    public String getImageThumbPath(ContentResolver contentResolver, int imageId) {
        String path = null;
        if (contentResolver == null) {
            return path;
        }
        Cursor thumbCursor = getImageThumbCursor(contentResolver, imageId);
        if (thumbCursor != null && thumbCursor.moveToFirst()) {
            if (mImageThumbDataColumnIndex < 0) {
                mImageThumbDataColumnIndex = thumbCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
            }
            path = thumbCursor.getString(mImageThumbDataColumnIndex);
            //Log.d("PicturePresenter","getImageThumbPath item.mThumbPath　isNotEmpty　imageId="+imageId+"　path=  "+path);
        } else {
            //Log.d("PicturePresenter","getImageThumbPath item.mThumbPath　isEmpty　imageId=  "+imageId);
        }
        if (thumbCursor != null) {
            thumbCursor.close();
        }
        return path;
    }


    //得到视频缩略图文件的Cursor
    public Cursor getVideoThumbCursor(ContentResolver contentResolver, int videoId) {

        if (contentResolver == null) {
            return null;
        }
        String[] thumbColumns = new String[]{
                MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID
        };
        String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=?";
        String[] selectionArgs = new String[]{
                videoId + ""
        };
        return contentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);

    }

    public Cursor getImageThumbCursor(ContentResolver contentResolver, int imageId) {

        if (contentResolver == null) {
            return null;
        }
        String[] thumbColumns = new String[]{
                MediaStore.Images.Thumbnails.DATA,
                MediaStore.Images.Thumbnails.IMAGE_ID
        };
        String selection = MediaStore.Images.Thumbnails.IMAGE_ID + "=?";
        String[] selectionArgs = new String[]{
                imageId + ""
        };
        return contentResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, thumbColumns, selection, selectionArgs, null);

    }

    public static String createVideoThumbPath(String videoUrl) {

        if (TextUtils.isEmpty(videoUrl)) {
            return null;
        }
        String thumbVideoName = String.valueOf(videoUrl.hashCode());
        thumbVideoName = "thumbVideoName" + thumbVideoName.replaceAll("-", "");
        String thumbImagePath = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE/pic/" + thumbVideoName + ".JPEG";
        File file = new File(thumbImagePath);
        MyLog.d(TAG, "PicDir = " + file.getPath());
        if (file.exists()) {
            //Log.d("PicturePresenter","createVideoThumbPath　file.exists　"+thumbImagePath);
            return file.getPath();
        }

        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoUrl, MediaStore.Images.Thumbnails.MINI_KIND);
        if (bitmap == null) {
            return null;
        }
        String result = savePicInLocalCertainPath(bitmap, thumbImagePath);
        if (!TextUtils.isEmpty(result)) {
            //Log.d("PicturePresenter","createVideoThumbPath　file. not exists　savePicInLocalCertainPath"+result);
        }

        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return result;
    }

    /**
     * 判断是否需要跳过该视频文件
     * @return true表示跳过, false表示不跳过
     */
    private static boolean needSkipThisVedio(String filePath, IVedioFileSkipFilter[] filters){
        if(filters != null){
            for(IVedioFileSkipFilter func : filters){
                if(func.isSkipVedioFile(filePath)){
                    return true;
                }
            }
        }

        return false;
    }


    public static String savePicInLocalCertainPath(final Bitmap bitmap, String path) {
        MyLog.d(TAG, "SavePicInLocal");
        if (bitmap == null || TextUtils.isEmpty(path)) {
            return null;
        }
        File file = new File(path);
        MyLog.d(TAG, "PicDir = " + file.getPath());
        if (file.exists()) {
            return file.getPath();
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        ByteArrayOutputStream baos = null; // 字节数组输出流
        try {
            baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] byteArray = baos.toByteArray();// 字节数组输出流转换成字节数组
            String saveDir = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE/pic";
            File dir = new File(saveDir);
            if (!dir.exists()) {
                dir.mkdir(); // 创建文件夹
            }

            file.createNewFile();// 创建文件
            MyLog.e("PicDir", file.getPath());

            // 将字节数组写入到刚创建的图片文件中
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(byteArray);
            MyLog.d(TAG, "保存成功 file.getPath()=" + file.getPath());
            return file.getPath();
        } catch (Exception e) {
            MyLog.e(TAG, "Exception = " + e);
            return null;
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 过滤不存在的文件
     */
    private static class NotExistVedioFileSkipFilter implements IVedioFileSkipFilter{
        public boolean isSkipVedioFile(String filePath){
            if(TextUtils.isEmpty(filePath)){
                return true;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                return true;
            }

            return false;
        }
    }

    /**
     * 根据文件后缀来过滤视频文件
     */
    private static class TypeVedioFileSkipFilter implements IVedioFileSkipFilter{
        public boolean isSkipVedioFile(String filePath){
            if(TextUtils.isEmpty(filePath)){
                return true;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                return true;
            }

            int indexOfDot = filePath.lastIndexOf('.');
            if (indexOfDot > 0 && indexOfDot < filePath.length()) {
                String extension = filePath.substring(indexOfDot + 1);
                MyLog.v(TAG + " isSkipVedioFile extension == " + extension);
                if (!TextUtils.isEmpty(extension) && extension.toLowerCase().equals("mp4")) {     //因为引擎只能播放和压缩mp4的格式
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * 根据文件大小来过滤视频文件
     */
    private static class SizeVedioFileSkipFilter implements IVedioFileSkipFilter{
        public boolean isSkipVedioFile(String filePath){
            if(TextUtils.isEmpty(filePath)){
                return true;
            }

            File file = new File(filePath);
            if (!file.exists()) {
                return true;
            }

            return file.length() >= MAX_VIDEO_FILE_SIZE;
        }
    }

    /**
     * 判断是否需要　过滤掉该视频文件
     */
    private static interface IVedioFileSkipFilter{
        public boolean isSkipVedioFile(String file);
    }
}
