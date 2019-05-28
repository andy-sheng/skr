package com.respicker.loader;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.MediaStore;

import com.common.base.BaseFragment;
import com.common.base.R;
import com.common.log.MyLog;
import com.common.utils.ImageUtils;
import com.common.utils.U;
import com.respicker.ResPicker;
import com.respicker.model.ResFolder;
import com.respicker.model.ImageItem;
import com.respicker.model.ResItem;
import com.respicker.model.VideoItem;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class ResDataSource {

    public final static String TAG = "ImageDataSource";

    private BaseFragment mFragment;
    private OnImagesLoadedListener mLoadedListener;                     //图片加载完成的回调接口
    private ContentResolver mPhotoAlbumContentResolver;

    private ContentObserver mPhotoAlbumContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            loadPhotoAlbum();
        }
    };

    private ContentResolver mVideoContentResolver;

    private ContentObserver mVideoContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            loadVideo();
        }
    };

    /**
     * @param fragment       用于初始化LoaderManager，需要兼容到2.3
     * @param loadedListener 图片加载完成的监听
     */
    public ResDataSource(BaseFragment fragment, OnImagesLoadedListener loadedListener) {
        this.mFragment = fragment;
        this.mLoadedListener = loadedListener;
    }

    public void destroy() {
        if (mPhotoAlbumContentResolver != null) {
            mPhotoAlbumContentResolver.unregisterContentObserver(mPhotoAlbumContentObserver);
        }
        if (mVideoContentResolver != null) {
            mVideoContentResolver.unregisterContentObserver(mVideoContentObserver);
        }
    }

    public ContentResolver getPhotoAlbumResolver() {
        if (mPhotoAlbumContentResolver == null) {
            mPhotoAlbumContentResolver = mFragment.getContext().getContentResolver();
            mPhotoAlbumContentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, mPhotoAlbumContentObserver);
        }
        return mPhotoAlbumContentResolver;
    }

    public ContentResolver getVideoResolver() {
        if (mVideoContentResolver == null) {
            mVideoContentResolver = mFragment.getContext().getContentResolver();
            mVideoContentResolver.registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, false, mVideoContentObserver);
        }
        return mVideoContentResolver;
    }

    public void loadRes() {
        Observable.create(new ObservableOnSubscribe<ArrayList<ResFolder>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<ResFolder>> emitter) throws Exception {
                ArrayList<ResFolder> resFolders = new ArrayList<>();
                ArrayList<ResItem> totalList = new ArrayList<>();
                ArrayList<ImageItem> imageItemList = loadPhotoAlbum();
                totalList.addAll(imageItemList);
                if (ResPicker.getInstance().getParams().isIncludeVideo()) {
                    ArrayList<VideoItem> videoList = loadVideo();
                    totalList.addAll(videoList);
                }
                Collections.sort(totalList, new Comparator<ResItem>() {
                    @Override
                    public int compare(ResItem o1, ResItem o2) {
                        if (o2.getAddTime() > o1.getAddTime()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                });

                for (ResItem resItem : totalList) {
                    String path = resItem.getPath();
                    File imageFile = new File(path);
                    File imageParentFile = imageFile.getParentFile();

                    ResFolder resFolder = new ResFolder();
                    resFolder.setName(imageParentFile.getName());
                    resFolder.setPath(imageParentFile.getAbsolutePath());

                    if (!resFolders.contains(resFolder)) {
                        if (resItem instanceof ImageItem) {
                            resFolder.setCover((ImageItem) resItem);
                        } else if (resItem instanceof VideoItem) {
                            VideoItem videoItem = (VideoItem) resItem;
                            resFolder.setCover(videoItem.getThumb());
                        }
                        resFolders.add(resFolder);
                    } else {
                        int index = resFolders.indexOf(resFolder);
                        resFolder = resFolders.get(index);
                    }
                    resFolder.getResItems().add(resItem);
                    if (resItem instanceof ImageItem) {
                        resFolder.getImageItems().add((ImageItem) resItem);
                    } else if (resItem instanceof VideoItem) {
                        VideoItem videoItem = (VideoItem) resItem;
                        resFolder.getVideoItems().add(videoItem);
                    }
                }

                //防止没有图片报异常
                if (totalList.size() > 0) {
                    //构造所有图片的集合
                    ResFolder allImagesFolder = new ResFolder();
                    allImagesFolder.setName(U.app().getResources().getString(R.string.ip_all_images));
                    allImagesFolder.setPath("/");
                    ResItem resItem = totalList.get(0);
                    if (resItem instanceof ImageItem) {
                        allImagesFolder.setCover((ImageItem) resItem);
                    } else if (resItem instanceof VideoItem) {
                        VideoItem videoItem = (VideoItem) resItem;
                        allImagesFolder.setCover(videoItem.getThumb());
                    }
                    allImagesFolder.getResItems().addAll(totalList);
                    for (ResItem r : totalList) {
                        if (r instanceof ImageItem) {
                            allImagesFolder.getImageItems().add((ImageItem) r);
                        } else if (r instanceof VideoItem) {
                            allImagesFolder.getVideoItems().add((VideoItem) r);
                        }
                    }
                    resFolders.add(0, allImagesFolder);  //确保第一条是所有图片
                }

                emitter.onNext(resFolders);
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.io())
                .compose(mFragment.bindUntilEvent(FragmentEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ArrayList<ResFolder>>() {
                    @Override
                    public void accept(ArrayList<ResFolder> resFolders) throws Exception {
                        if (mLoadedListener != null) {
                            ResPicker.getInstance().setResFolders(resFolders);
                            mLoadedListener.onImagesLoaded(resFolders);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        MyLog.e(throwable);
                    }
                });

    }

    private ArrayList<ImageItem> loadPhotoAlbum() {
        String[] image_projection = {     //查询图片需要的数据列
                MediaStore.Images.Media.DISPLAY_NAME,   //图片的显示名称  aaa.jpg
                MediaStore.Images.Media.DATA,           //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
                MediaStore.Images.Media.SIZE,           //图片的大小，long型  132492
                MediaStore.Images.Media.WIDTH,          //图片的宽度，int型  1920
                MediaStore.Images.Media.HEIGHT,         //图片的高度，int型  1080
                MediaStore.Images.Media.MIME_TYPE,      //图片的类型     image/jpeg
                MediaStore.Images.Media.DATE_ADDED     //图片被添加的时间，long型  1450518608
        };
        String selections = null;
        String[] selectionArgs = null;
        if (ResPicker.getInstance().getParams().isIncludeGif()) {

        } else {
            // 不要gif
            selections = new StringBuilder()
                    .append(MediaStore.Images.Media.MIME_TYPE)
                    .append("!=?")
                    .append(" and ")
                    .append(MediaStore.Images.Media.MIME_TYPE)
                    .append("!=?")
                    .toString();
            selectionArgs = new String[]{
                    "image/gif",
                    "image/webp"
            };
        }
        Cursor data = getPhotoAlbumResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, image_projection, selections, selectionArgs, image_projection[6] + " DESC");
        ArrayList<ImageItem> allImages = new ArrayList<>();   //所有图片的集合,不分文件夹
        if (data != null) {
            int i0 = data.getColumnIndexOrThrow(image_projection[0]);
            int i1 = data.getColumnIndexOrThrow(image_projection[1]);
            int i2 = data.getColumnIndexOrThrow(image_projection[2]);
            int i3 = data.getColumnIndexOrThrow(image_projection[3]);
            int i4 = data.getColumnIndexOrThrow(image_projection[4]);
            int i5 = data.getColumnIndexOrThrow(image_projection[5]);
            int i6 = data.getColumnIndexOrThrow(image_projection[6]);
            while (data.moveToNext()) {
                //查询数据
                String imageName = data.getString(i0);
                String imagePath = data.getString(i1);
                File file = new File(imagePath);
                if (!file.exists() || file.length() <= 0) {
                    continue;
                }
                long imageSize = data.getLong(i2);
                int imageWidth = data.getInt(i3);
                int imageHeight = data.getInt(i4);
                String imageMimeType = data.getString(i5);
                long imageAddTime = data.getLong(i6);
                //封装实体
                ImageItem imageItem = new ImageItem();
                imageItem.setName(imageName);
                imageItem.setPath(imagePath);
                imageItem.setSize(imageSize);
                imageItem.setWidth(imageWidth);
                imageItem.setHeight(imageHeight);
                imageItem.setMimeType(imageMimeType);
                imageItem.setAddTime(imageAddTime);

                // 再根据真正的类型判断一下是不是gif
                if (ResPicker.getInstance().getParams().isIncludeGif()) {
                    allImages.add(imageItem);
                } else{
                    if(U.getImageUtils().getImageType(imagePath) == ImageUtils.TYPE.GIF){

                    }else{
                        allImages.add(imageItem);
                    }
                }
            }
            data.close();
        }
        return allImages;
    }

    private ArrayList<VideoItem> loadVideo() {
        String[] video_projection = {     //查询视频需要的数据列
                MediaStore.Video.Media.DISPLAY_NAME,   //视频的显示名称  aaa.jpg
                MediaStore.Video.Media.DATA,           //视频的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
                MediaStore.Video.Media.SIZE,           //视频的大小，long型  132492
                MediaStore.Video.Media.WIDTH,          //视频的宽度，int型  1920
                MediaStore.Video.Media.HEIGHT,         //视频的高度，int型  1080
                MediaStore.Video.Media.MIME_TYPE,      //视频的类型     image/jpeg
                MediaStore.Video.Media.DATE_ADDED,     //视频被添加的时间，long型  1450518608
                MediaStore.Video.Media._ID     //视频的id
        };

        String selections = null;
        String[] selectionArgs = null;
        Cursor data = getVideoResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, video_projection, selections, selectionArgs, video_projection[6] + " DESC");
        ArrayList<VideoItem> allVideos = new ArrayList<>();   //所有图片的集合,不分文件夹
        if (data != null) {
            int i0 = data.getColumnIndexOrThrow(video_projection[0]);
            int i1 = data.getColumnIndexOrThrow(video_projection[1]);
            int i2 = data.getColumnIndexOrThrow(video_projection[2]);
            int i3 = data.getColumnIndexOrThrow(video_projection[3]);
            int i4 = data.getColumnIndexOrThrow(video_projection[4]);
            int i5 = data.getColumnIndexOrThrow(video_projection[5]);
            int i6 = data.getColumnIndexOrThrow(video_projection[6]);
            int i7 = data.getColumnIndexOrThrow(video_projection[7]);
            while (data.moveToNext()) {
                //查询数据
                String imageName = data.getString(i0);
                String imagePath = data.getString(i1);

                File file = new File(imagePath);
                if (!file.exists() || file.length() <= 0) {
                    continue;
                }

                long imageSize = data.getLong(i2);
                int imageWidth = data.getInt(i3);
                int imageHeight = data.getInt(i4);
                String imageMimeType = data.getString(i5);
                long imageAddTime = data.getLong(i6);
                int videoId = data.getInt(i7);

                VideoItem videoItem = new VideoItem();

                videoItem.setVideoId(videoId);
                videoItem.setName(imageName);
                videoItem.setPath(imagePath);
                videoItem.setSize(imageSize);
                videoItem.setWidth(imageWidth);
                videoItem.setHeight(imageHeight);
                videoItem.setMimeType(imageMimeType);
                videoItem.setAddTime(imageAddTime);

                String[] thumbColumns = {
                        MediaStore.Video.Thumbnails.DATA,           //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
                        MediaStore.Video.Thumbnails.WIDTH,          //图片的宽度，int型  1920
                        MediaStore.Video.Thumbnails.HEIGHT,         //图片的高度，int型  1080
                        MediaStore.Video.Thumbnails.VIDEO_ID,         //图片的高度，int型  1080
                };

                Cursor thumbCursor = mVideoContentResolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                        thumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID
                                + "=" + videoId, null, null);
                if (thumbCursor.moveToFirst()) {
                    int j0 = thumbCursor.getColumnIndexOrThrow(thumbColumns[0]);
                    int j1 = thumbCursor.getColumnIndexOrThrow(thumbColumns[1]);
                    int j2 = thumbCursor.getColumnIndexOrThrow(thumbColumns[2]);
                    int j3 = thumbCursor.getColumnIndexOrThrow(thumbColumns[3]);

                    ImageItem imageItem = new ImageItem();
                    imageItem.setPath(thumbCursor.getString(j0));
                    imageItem.setWidth(thumbCursor.getInt(j1));
                    imageItem.setHeight(thumbCursor.getInt(j2));

                    videoItem.setThumb(imageItem);
                }
                allVideos.add(videoItem);
            }
        }
        return allVideos;
    }

    /**
     * 所有图片加载完成的回调接口
     */
    public interface OnImagesLoadedListener {
        void onImagesLoaded(List<ResFolder> imageFolders);
    }
}
