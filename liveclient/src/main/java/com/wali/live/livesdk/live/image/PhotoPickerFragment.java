package com.wali.live.livesdk.live.image;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.activity.BaseRotateSdkActivity;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.repository.DataType.PhotoFolder;
import com.mi.milink.sdk.base.CustomHandlerThread;
import com.trello.rxlifecycle.FragmentEvent;
import com.wali.live.common.action.PickerAction;
import com.wali.live.common.listener.OnItemClickListener;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.image.adapter.FilePickerRecyclerAdapter;
import com.wali.live.livesdk.live.image.adapter.PhotoPickerRecyclerAdapter;
import com.wali.live.livesdk.live.utils.ImageUtils;
import com.wali.live.livesdk.live.view.SlidingTabLayout;
import com.base.view.SymmetryTitleBar;
import com.wali.live.livesdk.live.viewmodel.PhotoItem;
import com.wali.live.watchsdk.adapter.CommonTabPagerAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.wali.live.livesdk.live.view.SlidingTabLayout.DISTRIBUTE_MODE_TAB_AS_DIVIDER;

/**
 * Created by zyh on 15-12-23.
 *
 * @module 选图页面
 */
public class PhotoPickerFragment extends RxFragment implements OnClickListener, PhotoPickerRecyclerAdapter.UpdatePhotoListener {
    private static final String TAG = PhotoPickerFragment.class.getSimpleName();

    public static final int REQUEST_CODE = GlobalData.getRequestCode();
    public static final int UI_TYPE_DEFAULT = 0;
    public static final int UI_TYPE_ADD_PHOTO = 1;//直播中add图片选图
    private int mUiType;
    private boolean mNeedClip;
    private boolean mIsClipOk;
    private boolean mIsLandAtFirst = false;

    public static final String EXTRA_MAX_SELECT_COUNT = "extra_max_select_count";
    public static final String EXTRA_SELECT_SET = "extra_select_set";
    public static final String EXTRA_UI_TYPE = "extra_ui_type";
    public static final String EXTRA_NEED_CLIP = "extra_need_clip";
    public static final String EXTRA_PREVIEW_END_TO_SEND = "extra_preview_end_to_send"; //　预览大图点击取消或者完成则直接退出选人

    public static final int MAX_SELECT_COUNT = 6;
    public static final int REQUEST_SELECT_PHOTO = 100;
    private static final int MODE_DEFAULT = 1;
    private static final int MODE_IMAGE = 2;
    private int mMode = MODE_DEFAULT;

    public static final int PHOTO_COLUMN = 3;
    private int mSelectMaxPhoto = MAX_SELECT_COUNT;

    private ViewGroup mViewPgerContainer;
    private ViewPager mPhotoPager;
    private CommonTabPagerAdapter mPhotoTabAdapter;

    private ViewGroup mPhotoViewContainer;
    private TextView mFolderTitle;
    private TextView mEmpty;

    //点击file进去显示的photo
    private RecyclerView mRecyclerView;
    private PhotoPickerRecyclerAdapter mRecyclerAdapter;
    private GridLayoutManager mLayoutManager;
    private List<PhotoItem> mPhotoInFileItems;

    private SlidingTabLayout mPhotoTab;
    private HashMap<String, PhotoItem> mSelectedMap = new HashMap<>();
    //photo
    private RecyclerView mPhotoRecyclerView;
    private PhotoPickerRecyclerAdapter mPhotoAdapter;
    private GridLayoutManager mPhotoGridManager;
    private Cursor mPhotoCursor;
    //file
    private RecyclerView mFileRecyclerView;
    private FilePickerRecyclerAdapter mFileAdapter;
    private LinearLayoutManager mFileLinearManger;
    private Cursor mFileCursor;

    /*头部控制区域*/
    private SymmetryTitleBar mTitleBar;

    private boolean mFragmentAnimationEnd = false;
    private String mTitleBarRightBtnText;
    private int mClipPhotoHeight;   // 进入裁剪图片，裁剪的高度

    //查看大图
    private OnItemClickListener mPhotoItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (mUiType == UI_TYPE_ADD_PHOTO) {
                if (mNeedClip) {
                    openClipActivity(position, mPhotoAdapter);
                } else {
                    actionFinish();
                }
            }
        }
    };

    //查看大图 mRecyclerAdapter
    private OnItemClickListener mPhotoInFileItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            if (mUiType == UI_TYPE_ADD_PHOTO) {
                if (mNeedClip) {
                    openClipActivity(position, mRecyclerAdapter);
                } else {
                    actionFinish();
                }
            }
        }
    };


    OnItemClickListener mOnFileItemClick = new OnItemClickListener() {
        @Override
        public void onItemClick(View view, int position) {
            MyLog.d(TAG, " mFileAdapter onItemClick");
            mFileCursor.moveToPosition(position);
            changeMode(MODE_IMAGE);
            String folderName = mFileCursor.getString(mFileCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
            if (!TextUtils.isEmpty(folderName) && mFolderTitle != null) {
                mFolderTitle.setText(folderName);
            }
            refreshPhoto(mFileCursor.getString(mFileCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID)));
        }
    };


    CustomHandlerThread mCustomHandlerThread = new CustomHandlerThread(TAG) {
        @Override
        protected void processMessage(Message msg) {
        }
    };

    private Handler mUiHandler = new Handler(Looper.getMainLooper());

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.photo_picker_fragment, container, false);
    }

    @Override
    protected void bindView() {
        initData();

        initPhotoView();
        initTopLayout();
    }

    private void initData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            mUiType = bundle.getInt(EXTRA_UI_TYPE, UI_TYPE_DEFAULT);
            if (mUiType == UI_TYPE_ADD_PHOTO) {
                mSelectMaxPhoto = 1;
                mTitleBarRightBtnText = "";
                mNeedClip = bundle.getBoolean(EXTRA_NEED_CLIP, false);
            } else {
                mSelectMaxPhoto = bundle.getInt(EXTRA_MAX_SELECT_COUNT, MAX_SELECT_COUNT);
            }
            mClipPhotoHeight = bundle.getInt(ClipImageActivity.CROP_IMAGE_HEIGHT, 0);
        }
    }

    private void initPhotoView() {
        mEmpty = (TextView) mRootView.findViewById(R.id.is_empty_view);
        if (BaseActivity.isMIUIV6()) {
            //miui的bug 全屏时候需要设置一下大小 要不mRootView的大小可能会超过屏幕带到9000px多
            ViewGroup.LayoutParams layoutParams = mRootView.getLayoutParams();

            if (DisplayUtils.getScreenHeight() < DisplayUtils.getScreenWidth()) {
                layoutParams.height = DisplayUtils.getScreenWidth();
                layoutParams.width = DisplayUtils.getScreenHeight();
            } else {
                layoutParams.height = DisplayUtils.getScreenHeight();
                layoutParams.width = DisplayUtils.getScreenWidth();
            }
            mRootView.setLayoutParams(layoutParams);
        }
        //photo
        mPhotoRecyclerView = new RecyclerView(getActivity());
        if (mUiType == UI_TYPE_ADD_PHOTO) {
            mPhotoAdapter = new PhotoPickerRecyclerAdapter(mSelectMaxPhoto, mUiType);
        } else {
            mPhotoAdapter = new PhotoPickerRecyclerAdapter(mSelectMaxPhoto);
        }
        mPhotoAdapter.setUpdateListener(this);
        mPhotoCursor = getAllPhotoCursor();

        //查看大图
        mPhotoAdapter.setOnItemClickListener(mPhotoItemClickListener);
        mPhotoRecyclerView.setAdapter(mPhotoAdapter);
        mPhotoGridManager = new GridLayoutManager(getActivity(), PHOTO_COLUMN);
        mPhotoRecyclerView.setLayoutManager(mPhotoGridManager);
        mPhotoRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mPhotoRecyclerView.setPadding(-3, -3, -3, -3);
        updatePhotoList();

        //file
        mFileRecyclerView = new RecyclerView(getActivity());
        mFileAdapter = new FilePickerRecyclerAdapter();
        mFileRecyclerView.setAdapter(mFileAdapter);
        mFileLinearManger = new LinearLayoutManager(getActivity());
        mFileRecyclerView.setLayoutManager(mFileLinearManger);
        mFileRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mFileRecyclerView.setHasFixedSize(true);
        mFileCursor = getAllPhotoLibCursor();
        mFileAdapter.setOnItemClickListener(mOnFileItemClick);
        updatePhotoFolderList();

        mViewPgerContainer = (ViewGroup) mRootView.findViewById(R.id.viewpager_container);
        mPhotoTab = (SlidingTabLayout) mRootView.findViewById(R.id.photo_tab);
        mPhotoPager = (ViewPager) mRootView.findViewById(R.id.section_pager);
        mPhotoTabAdapter = new CommonTabPagerAdapter();
        mPhotoTabAdapter.addView(getString(R.string.photo), mPhotoRecyclerView);
        mPhotoTabAdapter.addView(getString(R.string.photo_file_list), mFileRecyclerView);
        mPhotoPager.setAdapter(mPhotoTabAdapter);

        mPhotoTab.setSelectedIndicatorColors(getResources().getColor(R.color.color_90_dcab42));
        mPhotoTab.setCustomTabView(R.layout.program_slide_tab_view, R.id.tab_tv);
        mPhotoTab.setDistributeMode(DISTRIBUTE_MODE_TAB_AS_DIVIDER);
        mPhotoTab.setIndicatorWidth(DisplayUtils.dip2px(12));
        mPhotoTab.setIndicatorBottomMargin(DisplayUtils.dip2px(4));
        mPhotoTab.setViewPager(mPhotoPager);

        //从file选择图片
        mPhotoViewContainer = (ViewGroup) mRootView.findViewById(R.id.photo_view_container);
        mFolderTitle = (TextView) mRootView.findViewById(R.id.folder_name);
        mFolderTitle.setTag(PickerAction.ACTION_BACK);
        mFolderTitle.setOnClickListener(this);

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        if (mUiType == UI_TYPE_ADD_PHOTO) {
            mRecyclerAdapter = new PhotoPickerRecyclerAdapter(mSelectMaxPhoto, mUiType);
        } else {
            mRecyclerAdapter = new PhotoPickerRecyclerAdapter(mSelectMaxPhoto);
        }
        mRecyclerAdapter.setUpdateListener(this);
        //查看大图
        mRecyclerAdapter.setOnItemClickListener(mPhotoInFileItemClickListener);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mLayoutManager = new GridLayoutManager(getActivity(), PHOTO_COLUMN);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
    }

    private void initTopLayout() {
        mTitleBar = (SymmetryTitleBar) mRootView.findViewById(R.id.title_bar);

        TextView leftTextBtn = mTitleBar.getLeftTextBtn();
        leftTextBtn.setText(R.string.cancel);
        leftTextBtn.setTag(PickerAction.ACTION_CANCEL);
        leftTextBtn.setOnClickListener(this);
        RelativeLayout.LayoutParams leftTextBtnLayout = (RelativeLayout.LayoutParams) leftTextBtn.getLayoutParams();
        leftTextBtnLayout.leftMargin = 30;
        leftTextBtn.setLayoutParams(leftTextBtnLayout);
        if (mUiType == UI_TYPE_ADD_PHOTO) {
            mTitleBar.setTitle(R.string.add_photo_pick_title);
        } else {
            mTitleBar.setTitle(getString(R.string.photo_lib_title, mSelectedMap == null ? 0 : mSelectedMap.size()));
            TextView rightTextBtn = mTitleBar.getRightTextBtn();
            mTitleBarRightBtnText = TextUtils.isEmpty(mTitleBarRightBtnText) ? getString(R.string.ok) : mTitleBarRightBtnText;
            rightTextBtn.setText(mTitleBarRightBtnText);
            changeLeftBtnStatus(mSelectedMap);
            rightTextBtn.setTag(PickerAction.ACTION_OK);
            RelativeLayout.LayoutParams rightTextBtnLayout = (RelativeLayout.LayoutParams) rightTextBtn.getLayoutParams();
            rightTextBtnLayout.rightMargin = 30;
            rightTextBtn.setLayoutParams(rightTextBtnLayout);
            rightTextBtn.setOnClickListener(this);
        }
    }

    private void updateTitleBar(Map selectMap) {
        if (mUiType != UI_TYPE_ADD_PHOTO) {
            mTitleBar.setTitle(getString(R.string.photo_lib_title, mSelectedMap == null ? 0 : mSelectedMap.size()));
            changeLeftBtnStatus(selectMap);
        }
    }

    @Override
    public void onClick(View v) {
        int action = 0;
        try {
            if (v.getTag() != null) {
                action = Integer.valueOf(String.valueOf(v.getTag()));
            }
        } catch (NumberFormatException e) {
            MyLog.d(TAG, e);
            return;
        }

        switch (action) {
            case PickerAction.ACTION_CANCEL:
                finish();
                break;
            case PickerAction.ACTION_OK:
                actionFinish();
                break;
            case PickerAction.ACTION_BACK:
                changeMode(MODE_DEFAULT);
                break;
        }
    }

    private void openClipActivity(int position, PhotoPickerRecyclerAdapter adapter) {
        if (mSelectedMap.size() > 0) {
            String path = new ArrayList<>(mSelectedMap.keySet()).get(0);
            //防止裁剪取消回来的处理
            mSelectedMap.clear();
            adapter.cancelSelectedItem(position);
            startCropActivity(Uri.parse(path));
        }
    }

    private void actionFinish() {
        if (mSelectedMap.size() > 0) {
            Bundle bundle = new Bundle();
            bundle.putSerializable(EXTRA_SELECT_SET, mSelectedMap);
            if (mDataListener != null) {
                mDataListener.onFragmentResult(mRequestCode, Activity.RESULT_OK, bundle);
            }
        }
        finish();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void changeMode(int mode) {
        this.mMode = mode;
        switch (mode) {
            case MODE_DEFAULT:
                mPhotoViewContainer.setVisibility(View.GONE);
                mRecyclerAdapter.clearPhotoList();
                mViewPgerContainer.setVisibility(View.VISIBLE);
                mPhotoAdapter.notifyDataSetChanged();
                break;
            case MODE_IMAGE:
                mPhotoViewContainer.setVisibility(View.VISIBLE);
                mViewPgerContainer.setVisibility(View.GONE);
                break;
        }
    }

    private Cursor getAllPhotoCursor() {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, sortOrder);
        return cursor;
    }

    private List<PhotoItem> getAllPhotoList(Cursor cursor) {
        List<PhotoItem> itemList = new ArrayList<>();
        try {
            if (cursor != null && !cursor.isClosed()) {
                int idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                int dataIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                PhotoItem photoItem;

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                while (cursor.moveToNext()) {
                    photoItem = new PhotoItem(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getString(idIndex)).toString());
                    photoItem.setLocalPath(cursor.getString(dataIndex));
                    if (mSelectedMap.containsKey(photoItem.getLocalPath())) {
                        photoItem.setSelected(true);
                    }
                    photoItem.setSrcSize(PhotoItem.RESIZE, PhotoItem.RESIZE);
                    itemList.add(photoItem);
                }
            }
        } catch (Exception e) {
            MyLog.e(e);
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return itemList;
    }

    private void updatePhotoList() {
        Observable.just(0)
                .map(new Func1<Integer, List<PhotoItem>>() {
                    @Override
                    public List<PhotoItem> call(Integer integer) {
                        return getAllPhotoList(mPhotoCursor);
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<List<PhotoItem>>bindUntilEvent(FragmentEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<PhotoItem>>() {
                    @Override
                    public void call(final List<PhotoItem> result) {
                        if (!isDetached()) {
                            if (result != null) {
                                mEmpty.setVisibility(View.GONE);
                                if (mFragmentAnimationEnd) {
                                    mPhotoAdapter.setPhotoList(result);
                                } else {
                                    mUiHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mPhotoAdapter.setPhotoList(result);
                                        }
                                    }, 300);
                                }
                            } else {
                                mEmpty.setVisibility(View.VISIBLE);
                                String errorMsg = getString(R.string.unknown_error);
                                MyLog.d(TAG, errorMsg);
                                ToastUtils.showToast(getActivity(), errorMsg);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "updatePhotoList() failed=" + throwable);
                    }
                });
    }

    private Cursor getAllPhotoLibCursor() {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {"count(" + MediaStore.Images.Media._ID + ")", MediaStore.Images.Media._ID, MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_ID, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        String selection = MediaStore.Images.Media.MIME_TYPE + " =? or " + MediaStore.Images.Media.MIME_TYPE + " =?) GROUP BY (" + MediaStore.Images.Media.BUCKET_ID;
        String[] selectionArgs = new String[]{"image/jpeg", "image/png"};
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC";
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);
        return cursor;
    }

    public List<PhotoFolder> getAllPhotoFolderList() {
        List<PhotoFolder> folderList = new ArrayList<>();
        try {
            if (mFileCursor != null && !mFileCursor.isClosed()) {
                while (mFileCursor.moveToNext()) {
                    PhotoFolder photoFolder = new PhotoFolder();
                    String folderName = mFileCursor.getString(mFileCursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    photoFolder.setFolderName(folderName);
                    String path = mFileCursor.getString(mFileCursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    photoFolder.setPhotoPath(path);
                    photoFolder.setPhotoCnt(mFileCursor.getInt(0));
                    folderList.add(photoFolder);
                }
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return folderList;
    }

    private void updatePhotoFolderList() {
        Observable.just(0)
                .map(new Func1<Integer, List<PhotoFolder>>() {
                    @Override
                    public List<PhotoFolder> call(Integer integer) {
                        return getAllPhotoFolderList();
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<List<PhotoFolder>>bindUntilEvent(FragmentEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<PhotoFolder>>() {
                    @Override
                    public void call(final List<PhotoFolder> result) {
                        if (!isDetached()) {
                            if (result != null) {
                                if (mFragmentAnimationEnd) {
                                    mFileAdapter.setFolderList(result);
                                } else {
                                    mUiHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mFileAdapter.setFolderList(result);
                                        }
                                    }, 300);
                                }
                            } else {
                                String errorMsg = getString(R.string.unknown_error);
                                MyLog.d(TAG, errorMsg);
                                ToastUtils.showToast(getActivity(), errorMsg);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "updatePhotoFolderList failed=" + throwable);
                    }
                });
    }

    private Cursor loadPhoto() {
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };
        String selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=? AND " + MediaStore.Images.Media.DATA + " like ?";
        String[] selectionArgs = {
                "Camera",
                "%" + Environment.DIRECTORY_DCIM + "%"
        };
        String sortOrder = MediaStore.Images.Media._ID + " DESC";
        return GlobalData.app().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    private Cursor loadPhoto(String bucket_id) {
        String[] projection = {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };

        String selection = MediaStore.Images.Media.BUCKET_ID + "=?";
        String[] selectionArgs = {
                bucket_id
        };
        String sortOrder = MediaStore.Images.Media._ID + " DESC";
        return GlobalData.app().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder);
    }

    private void refreshPhoto(final String bucket_id) {
        Observable.just(0)
                .map(new Func1<Integer, List<PhotoItem>>() {
                    @Override
                    public List<PhotoItem> call(Integer integer) {
                        Cursor cursor = null;
                        if (TextUtils.isEmpty(bucket_id)) {
                            cursor = loadPhoto();
                            if (cursor != null) {
                                List<PhotoItem> items = getAllPhotoList(cursor);
                                return items;
                            }
                        } else {
                            cursor = loadPhoto(bucket_id);
                            if (cursor != null) {
                                List<PhotoItem> items = getAllPhotoList(cursor);
                                return items;
                            }
                        }
                        return null;
                    }
                }).subscribeOn(Schedulers.io())
                .compose(this.<List<PhotoItem>>bindUntilEvent(FragmentEvent.DESTROY))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<PhotoItem>>() {
                    @Override
                    public void call(final List<PhotoItem> result) {
                        if (!isDetached()) {
                            if (null != result) {
                                mPhotoInFileItems = result;//标记File文件里的photoCursor
                                mRecyclerAdapter.setPhotoList(mPhotoInFileItems);
                            } else {
                                String errorMsg = getString(R.string.unknown_error);
                                ToastUtils.showToast(getActivity(), errorMsg);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "updatePhotoFolderList failed=" + throwable);
                    }
                });
    }

    public void addPhotoItem(final PhotoItem photoItem) {
        if (photoItem.getSrcWidth() == PhotoItem.RESIZE && photoItem.getSrcHeight() == PhotoItem.RESIZE) {
            mCustomHandlerThread.post(new Runnable() {
                @Override
                public void run() {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(photoItem.getLocalPath(), options);
                    photoItem.setSrcSize(options.outWidth, options.outHeight);
                }
            });
        }
        mSelectedMap.put(photoItem.getLocalPath(), photoItem);
        updateTitleBar(mSelectedMap);
    }

    public void removePhotoItem(PhotoItem photoItem) {
        mSelectedMap.remove(photoItem.getLocalPath());
        updateTitleBar(mSelectedMap);
    }

    public int getSelectedSize() {
        return mSelectedMap.size();
    }

    public HashMap<String, PhotoItem> getSelectItem() {
        return mSelectedMap;
    }

    private void finish() {
        MyLog.w(TAG, "finish");
        FragmentNaviUtils.popFragmentFromStack(getActivity());
    }

    @Override
    public boolean onBackPressed() {
        MyLog.d(TAG, " onBackPressed ");

        if (mMode == MODE_DEFAULT) {
            finish();
            return true;
        } else {
            changeMode(MODE_DEFAULT);
            return true;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof BaseRotateSdkActivity) {
            int orientation = ((BaseRotateSdkActivity) getActivity()).getScreenOrientation();
            mIsLandAtFirst = ((BaseRotateSdkActivity) getActivity()).isLandscape(orientation);
        }
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (mIsLandAtFirst) {
            return null;
        }
        Animation anim = null;
        try {
            if (nextAnim == 0) {
                if (enter) {
                    nextAnim = R.anim.slide_right_in;
                } else {
                    nextAnim = R.anim.slide_right_out;
                }
            }
            anim = AnimationUtils.loadAnimation(getActivity(), nextAnim);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mFragmentAnimationEnd = true;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        } catch (Resources.NotFoundException e) {
            MyLog.e(e);
        } catch (Exception e) {
            MyLog.e(e);
        }

        MyLog.w(TAG, "onCreateAnimation mNeedClip" + mNeedClip);
        if (mIsClipOk) {
            return null;
        } else {
            return anim;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPhotoCursor != null && !mPhotoCursor.isClosed()) {
            mPhotoCursor.close();
        }
        if (mFileCursor != null && !mFileCursor.isClosed()) {
            mFileCursor.close();
        }
        if (mCustomHandlerThread != null) {
            mCustomHandlerThread.destroy();
        }
        mUiHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MyLog.w(TAG, "onActivityResult requestCode : " + requestCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case ClipImageActivity.REQUEST_CODE_CROP:
                mIsClipOk = true;
                MyLog.w(TAG, "mIsClipOk: " + mIsClipOk);
                Bundle bundle = new Bundle();
                String action = (data == null) ? "" : data.getAction();
                Uri tempUri = Uri.parse(action);
                if (tempUri != null) {
                    bundle.putSerializable(ClipImageActivity.SAVE_CLIP_IMAGE_PATH, tempUri.getPath());
                }
                if (mDataListener != null) {
                    mDataListener.onFragmentResult(ClipImageActivity.REQUEST_CODE_CROP, Activity.RESULT_OK, bundle);
                }
                finish();
                break;
            default:
                break;
        }
    }

    /**
     * 启动裁剪图片的activity
     *
     * @param uri
     */
    private void startCropActivity(final Uri uri) {
        MyLog.w(TAG, "startCropActivity uri=" + uri);
        //创建文件夹
        final String dirPath = Environment.getExternalStorageDirectory() + ImageUtils.AVATAR_TEMP_DIR;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String cropTempFile = dirPath + "cropTemp" + System.currentTimeMillis() + ".jpg";
        File cropTmpFile = new File(cropTempFile);
        //启动裁剪activity
        final Intent cropIntent = new Intent(getActivity(), ClipImageActivity.class);
        cropIntent.setDataAndType(uri, "image/*");
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cropTmpFile));
        if (mClipPhotoHeight != 0) {
            cropIntent.putExtra(ClipImageActivity.CROP_IMAGE_HEIGHT, mClipPhotoHeight);
        }
        startActivityForResult(cropIntent, ClipImageActivity.REQUEST_CODE_CROP);
        MyLog.w("PhotoPicker, mCurrentSavePath is: " + cropTempFile);
    }

    public static void openFragment(BaseActivity fragmentActivity, FragmentDataListener listener, int selectMaxPhoto) {
        if (PermissionUtils.checkSdcardAlertWindow(fragmentActivity)) {
            KeyboardUtils.hideKeyboard(fragmentActivity);
            Bundle bundle = new Bundle();
            bundle.putInt(PhotoPickerFragment.EXTRA_MAX_SELECT_COUNT, selectMaxPhoto);
            BaseFragment fragment = FragmentNaviUtils.addFragment(fragmentActivity, R.id.main_act_container, PhotoPickerFragment.class, bundle, true, false, true);
            fragment.initDataResult(REQUEST_SELECT_PHOTO, listener);
        } else {
            PermissionUtils.requestPermissionDialog(fragmentActivity, PermissionUtils.PermissionType.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void changeLeftBtnStatus(Map selectMap) {
        mTitleBar.getRightTextBtn().setEnabled(!(selectMap == null || selectMap.size() == 0));
    }

    @Override
    public String getTAG() {
        return TAG;
    }

    @Override
    public boolean isStatusBarDark() {
        return true;
    }

    @Override
    public boolean isOverrideStatusBar() {
        return true;
    }

    public static void openFragment(BaseActivity activity, FragmentDataListener listener, Bundle bundle) {
        if (PermissionUtils.checkSdcardAlertWindow(activity)) {
            KeyboardUtils.hideKeyboard(activity);
            BaseFragment fragment = FragmentNaviUtils.addFragment(activity, R.id.main_act_container, PhotoPickerFragment.class, bundle, true, false, true);
            fragment.initDataResult(REQUEST_SELECT_PHOTO, listener);
        } else {
            PermissionUtils.requestPermissionDialog(activity, PermissionUtils.PermissionType.WRITE_EXTERNAL_STORAGE);
        }
    }
}
