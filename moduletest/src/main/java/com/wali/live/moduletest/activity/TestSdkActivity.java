package com.wali.live.moduletest.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.common.core.RouterConstants;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfo;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.player.VideoPlayerAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.NetworkUtils;
import com.common.utils.PermissionUtil;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.example.drawer.DrawerFragment;
import com.example.paginate.PaginateFragment;
import com.imagepicker.ImagePicker;
import com.imagepicker.fragment.ImagePickerFragment;
import com.imagepicker.fragment.ImagePreviewFragment;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.CropImageView;
import com.wali.live.modulechannel.IChannelService;
import com.wali.live.moduletest.R;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Route(path = RouterConstants.ACTIVITY_TEST)
public class TestSdkActivity extends BaseActivity {
    CommonTitleBar mTitlebar;
    RecyclerView mListRv;
    List<H> mDataList = new ArrayList<>();

    Handler mUiHanlder = new Handler();

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.test_main_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) findViewById(R.id.titlebar);

        mTitlebar.getCenterTextView().setText(MyUserInfoManager.getInstance().getNickName());
        View view = mTitlebar.getLeftCustomView();
        BaseImageView baseImageView = view.findViewById(R.id.head_img);

        AvatarUtils.loadAvatarByUrl(baseImageView,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getUid())
                        .setTimestamp(MyUserInfoManager.getInstance().getAvatar())
                        .build());

        mListRv = (RecyclerView) findViewById(R.id.list_rv);

        mListRv.setLayoutManager(new LinearLayoutManager(this));
        mListRv.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.test_item_tv, parent, false);
                TestHolder testHolder = new TestHolder(view);
                return testHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof TestHolder) {
                    TestHolder testHolder = (TestHolder) holder;
                    testHolder.bindData(mDataList.get(position));
                }
            }

            @Override
            public int getItemCount() {
                return mDataList.size();
            }
        });

        mDataList.add(new H("跳转到LoginActivity", new Runnable() {

            @Override
            public void run() {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_LOGIN)
                        .greenChannel()
                        .navigation(TestSdkActivity.this, new NavigationCallback() {
                            @Override
                            public void onFound(Postcard postcard) {

                            }

                            @Override
                            public void onLost(Postcard postcard) {

                            }

                            @Override
                            public void onArrival(Postcard postcard) {

                            }

                            @Override
                            public void onInterrupt(Postcard postcard) {

                            }
                        });
            }
        }));

        mDataList.add(new H("跳转到ChannelListSdkActivity", new Runnable() {
            @Override
            public void run() {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_CHANNEL_LIST_SDK).greenChannel().navigation(TestSdkActivity.this, new NavigationCallback() {
                    @Override
                    public void onFound(Postcard postcard) {

                    }

                    @Override
                    public void onLost(Postcard postcard) {

                    }

                    @Override
                    public void onArrival(Postcard postcard) {

                    }

                    @Override
                    public void onInterrupt(Postcard postcard) {

                    }
                });
            }
        }));

        mDataList.add(new H("跳转到WatchSdkActivity", new Runnable() {
            @Override
            public void run() {
                VideoPlayerAdapter.preStartPlayer("http://playback.ks.zb.mi.com/record/live/101743_1531094545/hls/101743_1531094545.m3u8?playui=1");
                //跳到LoginActivity,要用ARouter跳
                ARouter.getInstance().build(RouterConstants.ACTIVITY_WATCH).navigation(TestSdkActivity.this, new NavigationCallback() {
                    @Override
                    public void onFound(Postcard postcard) {
                        MyLog.d(TAG, "onFound" + " postcard=" + postcard);
                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        MyLog.d(TAG, "onLost" + " postcard=" + postcard);
                    }

                    @Override
                    public void onArrival(Postcard postcard) {
                        MyLog.d(TAG, "onArrival" + " postcard=" + postcard);
                    }

                    @Override
                    public void onInterrupt(Postcard postcard) {
                        MyLog.d(TAG, "onInterrupt" + " postcard=" + postcard);
                    }
                });
            }
        }));


        mDataList.add(new H("VirtualApk load 测试", new Runnable() {
            @Override
            public void run() {
                String pluginPath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/Test.apk");
                File plugin = new File(pluginPath);
//                try {
//                    // load 会导致 Applicaiton 加载两次，看原理
//                    com.didi.virtualapk.PluginManager.PluginManager.getInstance(U.app()).loadPlugin(plugin);
//                    U.getToastUtil().showToast("load 成功");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    U.getToastUtil().showToast("load 失败");
//                }
            }
        }));

        mDataList.add(new H("VirtualApk 跳转 测试", new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.setClassName("com.wali.live.pldemo", "com.wali.live.pldemo.activity.PDMainAcitivity");
                startActivity(intent);
            }
        }));

        mDataList.add(new H("DroidPlugin 专项调试", new Runnable() {
            @Override
            public void run() {
                ARouter.getInstance().build("/test/DroidPluginTestAcitivity").greenChannel().navigation(TestSdkActivity.this, new NavigationCallback() {
                    @Override
                    public void onFound(Postcard postcard) {

                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        U.getToastUtil().showToast("请确认 gradle.properties 中 droidpluginEnable 的开关是否打开");
                    }

                    @Override
                    public void onArrival(Postcard postcard) {

                    }

                    @Override
                    public void onInterrupt(Postcard postcard) {

                    }
                });
            }
        }));

        mDataList.add(new H("Replugin 专项调试", new Runnable() {
            @Override
            public void run() {
                ARouter.getInstance().build("/test/RepluginTestAcitivity").greenChannel().navigation(TestSdkActivity.this, new NavigationCallback() {
                    @Override
                    public void onFound(Postcard postcard) {

                    }

                    @Override
                    public void onLost(Postcard postcard) {
                        U.getToastUtil().showToast("请确认 gradle.properties 中 repluginEnable 的开关是否打开");
                    }

                    @Override
                    public void onArrival(Postcard postcard) {

                    }

                    @Override
                    public void onInterrupt(Postcard postcard) {

                    }
                });
            }
        }));


        mDataList.add(new H("判断5s后app是否在前台", new Runnable() {
            @Override
            public void run() {
                AvatarUtils.loadAvatarByUrl(baseImageView,
                        AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getUid())
                                .setTimestamp(MyUserInfoManager.getInstance().getAvatar())
                                .build());

                U.getAppInfoUtils().showDebugDBAddressLogToast();
                mUiHanlder.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        U.getToastUtil().showToast("在前台 " + U.getActivityUtils().isAppForeground());
                    }
                }, 5000);
            }
        }));

        mDataList.add(new H("上拉加载,下拉刷新的RecyclerView Panigate库调试 ", new Runnable() {
            @Override
            public void run() {
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newParamsBuilder(TestSdkActivity.this, PaginateFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        }));

        mDataList.add(new H("官方库 抽屉DrawerLayout 导航栏 NavigationView调试", new Runnable() {
            @Override
            public void run() {
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newParamsBuilder(TestSdkActivity.this, DrawerFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        }));

        mDataList.add(new H("ARouter 依赖注入测试，访问其他Module 数据", new Runnable() {
            @Override
            public void run() {
                IChannelService channelService = (IChannelService) ARouter.getInstance().build(RouterConstants.SERVICE_CHANNEL).navigation();
                if (channelService != null) {
                    Object object = channelService.getDataFromChannel(100, null);
                    U.getToastUtil().showToast("test module 收到数据 object:" + object + " hash:" + channelService.hashCode());
                }
            }
        }));

        mDataList.add(new H("ImagePicker调试", new Runnable() {
            @Override
            public void run() {
                Bundle bundle = new Bundle();
                ImagePicker.getInstance().setParams(ImagePicker.newParamsBuilder()
                        .setSelectLimit(8)
                        .setCropStyle(CropImageView.Style.CIRCLE)
                        .build()
                );
                U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(TestSdkActivity.this, ImagePickerFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setBundle(bundle)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
                                U.getToastUtil().showToast("拿到数据 size:" + ImagePicker.getInstance().getSelectedImages().size());
                            }
                        })
                        .build());
            }
        }));


        mDataList.add(new H("ImagePreview调试，大图", new Runnable() {
            @Override
            public void run() {
                String ps[] = new String[]{
                        "http://img.zcool.cn/community/01259e59798aa4a8012193a3c94637.gif"
                        , "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1540971147&di=bcc5a2a15cd48731be2b020c90b84414&imgtype=jpg&er=1&src=http%3A%2F%2Fa.vpimg2.com%2Fupload%2Fmerchandise%2Fpdc%2F736%2F961%2F9013468006181961736%2F1%2FRwhr254407-6.jpg"
                        , "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1540376427777&di=1ec1e64f7a022e0ce371bb2c0c142989&imgtype=0&src=http%3A%2F%2Fimg0.ph.126.net%2FbYB8CJTnruqbgKzEFuRXEg%3D%3D%2F6632030937887660874.jpg"
                        , "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1540376427776&di=6441e0f1b67858eae6560dc10de05bae&imgtype=0&src=http%3A%2F%2Fimg.alicdn.com%2Fimgextra%2Fi3%2F2337431051%2FTB2dBlOepXXXXXiXpXXXXXXXXXX_%2521%25212337431051.jpg"
                        , "https://ss0.bdstatic.com/70cFvHSh_Q1YnxGkpoWK1HF6hhy/it/u=1664883472,2674356486&fm=26&gp=0.jpg"
                        , "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1540391118854&di=4db867ada9dfa74ebc75b488f5129722&imgtype=0&src=http%3A%2F%2Fimg.zcool.cn%2Fcommunity%2F014d4458ca8c2ea801219c7787a209.gif"
                        , "/sdcard/1.gif"
                        , "/sdcard/1.jpeg"

                };
                List<ImageItem> list = new ArrayList<>();
                for (String s : ps) {
                    ImageItem imageItem = new ImageItem();
                    imageItem.setPath(s);
                    list.add(imageItem);
                }

                U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(TestSdkActivity.this, ImagePreviewFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .setDataBeforeAdd(1, list)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
                                U.getToastUtil().showToast("拿到数据 size:" + ImagePicker.getInstance().getSelectedImages().size());
                            }
                        })
                        .build());
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!U.getPermissionUtils().checkExternalStorage(this)) {
            U.getPermissionUtils().requestExternalStorage(new PermissionUtil.RequestPermission() {
                @Override
                public void onRequestPermissionSuccess() {
                    MyLog.d(TAG, "onRequestPermissionSuccess");
                }

                @Override
                public void onRequestPermissionFailure(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailure" + " permissions=" + permissions);
                }

                @Override
                public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                    MyLog.d(TAG, "onRequestPermissionFailureWithAskNeverAgain" + " permissions=" + permissions);
                }
            }, this);
        }
    }

    @Subscribe
    public void onEvent(NetworkUtils.NetworkChangeEvent event) {
        U.getToastUtil().showToast("网络变化 now:" + event.type);
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public boolean canSlide() {
        return false;
    }

    static class H {
        public String title;
        public Runnable op;

        public H(String title, Runnable op) {
            this.title = title;
            this.op = op;
        }
    }

    static class TestHolder extends RecyclerView.ViewHolder {

        TextView titleTv;
        H data;

        public TestHolder(View itemView) {
            super(itemView);

            titleTv = (TextView) itemView.findViewById(R.id.desc_tv);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (data != null) {
                        data.op.run();
                    }
                }
            });
        }

        public void bindData(H data) {
            this.data = data;
            titleTv.setText(data.title);
        }
    }
}
