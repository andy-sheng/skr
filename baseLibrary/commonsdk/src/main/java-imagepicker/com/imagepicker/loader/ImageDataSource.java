package com.imagepicker.loader;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.MediaStore;

import com.common.base.BaseFragment;
import com.common.base.R;
import com.common.utils.U;
import com.imagepicker.ImagePicker;
import com.imagepicker.model.ImageFolder;
import com.imagepicker.model.ImageItem;
import com.trello.rxlifecycle2.android.FragmentEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：加载手机图片实现类
 * 修订历史：
 * ================================================
 */
public class ImageDataSource {

    public final static String TAG = "ImageDataSource";

    public static final int LOADER_ALL = 0;         //加载所有图片
    private final String[] IMAGE_PROJECTION = {     //查询图片需要的数据列
            MediaStore.Images.Media.DISPLAY_NAME,   //图片的显示名称  aaa.jpg
            MediaStore.Images.Media.DATA,           //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            MediaStore.Images.Media.SIZE,           //图片的大小，long型  132492
            MediaStore.Images.Media.WIDTH,          //图片的宽度，int型  1920
            MediaStore.Images.Media.HEIGHT,         //图片的高度，int型  1080
            MediaStore.Images.Media.MIME_TYPE,      //图片的类型     image/jpeg
            MediaStore.Images.Media.DATE_ADDED};    //图片被添加的时间，long型  1450518608

    private BaseFragment mFragment;
    private OnImagesLoadedListener loadedListener;                     //图片加载完成的回调接口
    private ContentResolver photoAlbumContentResolver;
    private ContentObserver photoAlbumContentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            load();
        }
    };

    /**
     * @param fragment       用于初始化LoaderManager，需要兼容到2.3
     * @param loadedListener 图片加载完成的监听
     */
    public ImageDataSource(BaseFragment fragment, OnImagesLoadedListener loadedListener) {
        this.mFragment = fragment;
        this.loadedListener = loadedListener;
        photoAlbumContentResolver = mFragment.getContext().getContentResolver();
        photoAlbumContentResolver.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, photoAlbumContentObserver);
    }

    public void destroy() {
        photoAlbumContentResolver.unregisterContentObserver(photoAlbumContentObserver);
    }

    public void load() {
        Observable.create(new ObservableOnSubscribe<ArrayList<ImageFolder>>() {
            @Override
            public void subscribe(ObservableEmitter<ArrayList<ImageFolder>> emitter) throws Exception {
                Cursor data = photoAlbumContentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[6] + " DESC");
                ArrayList<ImageFolder> imageFolders = new ArrayList<>();
                if (data != null) {
                    ArrayList<ImageItem> allImages = new ArrayList<>();   //所有图片的集合,不分文件夹
                    int i0 = data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]);
                    int i1 = data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]);
                    int i2 = data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]);
                    int i3 = data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]);
                    int i4 = data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]);
                    int i5 = data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]);
                    int i6 = data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]);
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
                        allImages.add(imageItem);
                        //根据父路径分类存放图片
                        File imageFile = new File(imagePath);
                        File imageParentFile = imageFile.getParentFile();
                        ImageFolder imageFolder = new ImageFolder();
                        imageFolder.setName(imageParentFile.getName());
                        imageFolder.setPath(imageParentFile.getAbsolutePath());

                        if (!imageFolders.contains(imageFolder)) {
                            ArrayList<ImageItem> images = new ArrayList<>();
                            images.add(imageItem);
                            imageFolder.setCover(imageItem);
                            imageFolder.setImages(images);
                            imageFolders.add(imageFolder);
                        } else {
                            imageFolders.get(imageFolders.indexOf(imageFolder)).getImages().add(imageItem);
                        }
                    }
                    //防止没有图片报异常
                    if (data.getCount() > 0 && allImages.size() > 0) {
                        //构造所有图片的集合
                        ImageFolder allImagesFolder = new ImageFolder();
                        allImagesFolder.setName(U.app().getResources().getString(R.string.ip_all_images));
                        allImagesFolder.setPath("/");
                        allImagesFolder.setCover(allImages.get(0));
                        allImagesFolder.setImages(allImages);
                        imageFolders.add(0, allImagesFolder);  //确保第一条是所有图片
                    }
                }

                emitter.onNext(imageFolders);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .compose(mFragment.bindUntilEvent(FragmentEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ArrayList<ImageFolder>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(ArrayList<ImageFolder> imageFolders) {
                        //回调接口，通知图片数据准备完成
                        ImagePicker.getInstance().setImageFolders(imageFolders);
                        loadedListener.onImagesLoaded(imageFolders);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }


    /**
     * 所有图片加载完成的回调接口
     */
    public interface OnImagesLoadedListener {
        void onImagesLoaded(List<ImageFolder> imageFolders);
    }
}
