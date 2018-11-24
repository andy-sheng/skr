package com.wali.live.moduletest.activity;

import android.content.Intent;
import android.net.Uri;
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

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.base.FragmentDataListener;
import com.module.RouterConstants;
import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.myinfo.event.MyUserInfoEvent;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.player.VideoPlayerAdapter;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.LbsUtils;
import com.common.utils.NetworkUtils;
import com.common.utils.PermissionUtils;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.example.dialog.DialogsFragment;
import com.example.drawer.DrawerFragment;
import com.example.emoji.EmojiFragment;
import com.example.qrcode.QrcodeTestFragment;
import com.example.rxretrofit.fragment.RxRetrofitFragment;
import com.example.smartrefresh.SmartRefreshFragment;
import com.example.wxcontact.PickContactFragment;
import com.imagepicker.ImagePicker;
import com.imagepicker.fragment.ImagePickerFragment;
import com.imagepicker.fragment.ImagePreviewFragment;
import com.imagepicker.model.ImageItem;
import com.imagepicker.view.CropImageView;
import com.module.home.IHomeService;
import com.pgyersdk.crash.PgyCrashManager;
import com.pgyersdk.feedback.PgyerFeedbackManager;
import com.pgyersdk.update.DownloadFileListener;
import com.pgyersdk.update.PgyUpdateManager;
import com.pgyersdk.update.UpdateManagerListener;
import com.pgyersdk.update.javabean.AppBean;
import com.wali.live.moduletest.H;
import com.wali.live.moduletest.R;
import com.wali.live.moduletest.TestViewHolder;
import com.wali.live.moduletest.fragment.ShowTextViewFragment;
import com.xiaomi.mistatistic.sdk.MiStatInterface;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    void loadAccountInfo() {
        if (UserAccountManager.getInstance().hasAccount()) {
            mTitlebar.getCenterTextView().setText(MyUserInfoManager.getInstance().getNickName());
        } else {
            mTitlebar.getCenterTextView().setText("未登陆");
        }
        View view = mTitlebar.getLeftCustomView();
        BaseImageView baseImageView = view.findViewById(R.id.head_img);

        AvatarUtils.loadAvatarByUrl(baseImageView,
                AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getUid())
                        .setTimestamp(MyUserInfoManager.getInstance().getAvatar())
                        .build());

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MyUserInfoEvent.UserInfoChangeEvent event) {
        loadAccountInfo();
    }


    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getToastUtil().setBgColor(getResources().getColor(R.color.blue));
        mTitlebar = (CommonTitleBar) findViewById(R.id.titlebar);
        loadAccountInfo();

        mListRv = (RecyclerView) findViewById(R.id.list_rv);

        mListRv.setLayoutManager(new LinearLayoutManager(this));
        mListRv.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.test_item_tv, parent, false);
                TestViewHolder testHolder = new TestViewHolder(view);
                return testHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                if (holder instanceof TestViewHolder) {
                    TestViewHolder testHolder = (TestViewHolder) holder;
                    testHolder.bindData(mDataList.get(position));
                }
            }

            @Override
            public int getItemCount() {
                return mDataList.size();
            }
        });

        mDataList.add(new H("进入首页", new Runnable() {
            @Override
            public void run() {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_HOME)
                        .navigation();
            }
        }));

        mDataList.add(new H("打开消息", new Runnable() {

            @Override
            public void run() {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_MESSAGE)
                        .navigation();
            }
        }));

        mDataList.add(new H("检查更新", new Runnable() {
            @Override
            public void run() {
                new PgyUpdateManager.Builder()
                        .setForced(false)                //设置是否强制更新
                        .setUserCanRetry(false)         //失败后是否提示重新下载
                        .setDeleteHistroyApk(true)     // 检查更新前是否删除本地历史 Apk
                        .register();
            }
        }));

        mDataList.add(new H("检查更新(自定义过程)", new Runnable() {
            @Override
            public void run() {
                new PgyUpdateManager.Builder()
                        .setForced(true)                //设置是否强制更新,非自定义回调更新接口此方法有用
                        .setUserCanRetry(false)         //失败后是否提示重新下载，非自定义下载 apk 回调此方法有用
                        .setDeleteHistroyApk(false)     // 检查更新前是否删除本地历史 Apk
                        .setUpdateManagerListener(new UpdateManagerListener() {
                            @Override
                            public void onNoUpdateAvailable() {
                                //没有更新是回调此方法
                                MyLog.d("pgyer", "there is no new version");
                                U.getToastUtil().showShort("没有更新的了");
                            }

                            @Override
                            public void onUpdateAvailable(AppBean appBean) {
                                //没有更新是回调此方法
                                MyLog.d("pgyer", "there is new version can update"
                                        + "new versionCode is " + appBean.getVersionCode());

                                //调用以下方法，DownloadFileListener 才有效；如果完全使用自己的下载方法，不需要设置DownloadFileListener
                                U.getToastUtil().showShort("有更新开始下载");
                                PgyUpdateManager.downLoadApk(appBean.getDownloadURL());
                            }

                            @Override
                            public void checkUpdateFailed(Exception e) {
                                //更新检测失败回调
                                MyLog.e("pgyer", "check update failed ", e);

                            }
                        })
                        //注意 ：下载方法调用 PgyUpdateManager.downLoadApk(appBean.getDownloadURL()); 此回调才有效
                        .setDownloadFileListener(new DownloadFileListener() {   // 使用蒲公英提供的下载方法，这个接口才有效。
                            @Override
                            public void downloadFailed() {
                                //下载失败
                                MyLog.e("pgyer", "download apk failed");
                            }

                            @Override
                            public void downloadSuccessful(Uri uri) {
                                MyLog.e("pgyer", "download apk failed");
                                // 默认存放的目录
                                // /storage/emulated/0/Android/data/com.zq.live/files/pgySdk/downloadApk/apk-1600434156.apk
                                PgyUpdateManager.installApk(uri);  // 使用蒲公英提供的安装方法提示用户 安装apk
                            }

                            @Override
                            public void onProgressUpdate(Integer... integers) {
                                MyLog.e("pgyer", "update download apk progress : " + integers[0]);
                            }
                        })
                        .register();

            }
        }));

        mDataList.add(new H("显示当前设备信息", new Runnable() {
            @Override
            public void run() {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_DEVICE_INFO)
                        .greenChannel()
                        .navigation();
            }
        }));


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

        boolean virtualapkLoad = false;
        mDataList.add(new H("VirtualApk load 测试", new Runnable() {
            @Override
            public void run() {
                String pluginPath = Environment.getExternalStorageDirectory().getAbsolutePath().concat("/Test.apk");
                File plugin = new File(pluginPath);
                Class cls = null;
                try {
                    cls = getClassLoader().loadClass("com.didi.virtualapk.PluginManager.PluginManager");
                } catch (ClassNotFoundException e) {

                }
                if (cls == null) {
                    U.getToastUtil().showShort("请确认 gradle.properties 中 virtualApkEnable 的开关是否打开");
                } else {
                    //                try {
//                    // load 会导致 Applicaiton 加载两次，看原理
//                    com.didi.virtualapk.PluginManager.PluginManager.getInstance(U.app()).loadPlugin(plugin);
//                    virtualapkLoad = true;
//                    U.getToastUtil().showShort("load 成功");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    U.getToastUtil().showShort("load 失败");
//                }
                }
            }
        }));

        mDataList.add(new H("VirtualApk 跳转 测试", new Runnable() {
            @Override
            public void run() {
                if (!virtualapkLoad) {
                    U.getToastUtil().showShort("virtualapkLoad == false");
                    return;
                }
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
//                        U.getToastUtil().showShort("请确认 gradle.properties 中 droidpluginEnable 的开关是否打开");
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
                        U.getToastUtil().showShort("请确认 gradle.properties 中 repluginEnable 的开关是否打开");
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

        mDataList.add(new H("强大的SmartRefreshLayout", new Runnable() {
            @Override
            public void run() {
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newParamsBuilder(TestSdkActivity.this, SmartRefreshFragment.class)
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

        mDataList.add(new H("浸入式 + CollapsingToolbarLayout 调试", new Runnable() {
            @Override
            public void run() {
            }
        }));


        mDataList.add(new H(" emoji表情面板 调试", new Runnable() {
            @Override
            public void run() {
                ARouter.getInstance().build(RouterConstants.ACTIVITY_EMOJI).greenChannel().navigation();
            }
        }));
        mDataList.add(new H(" emoji表情面板 调试2", new Runnable() {
            @Override
            public void run() {
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newParamsBuilder(TestSdkActivity.this, EmojiFragment.class)
                        .setAddToBackStack(true)
                        .build());
            }
        }));

        mDataList.add(new H("支持 shape的TextView & Span测试", new Runnable() {
            @Override
            public void run() {
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newParamsBuilder(TestSdkActivity.this, ShowTextViewFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        }));

        mDataList.add(new H("ARouter 依赖注入测试，访问其他Module 数据", new Runnable() {
            @Override
            public void run() {
                IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();
                if (channelService != null) {
                    Object object = channelService.getData(100, null);
                    U.getToastUtil().showShort("test module 收到数据 object:" + object + " hash:" + channelService.hashCode());
                }
            }
        }));

        mDataList.add(new H("DialogPlus 库调试", new Runnable() {
            @Override
            public void run() {
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newParamsBuilder(TestSdkActivity.this, DialogsFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
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
                                U.getToastUtil().showShort("拿到数据 size:" + ImagePicker.getInstance().getSelectedImages().size());
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
                                U.getToastUtil().showShort("拿到数据 size:" + ImagePicker.getInstance().getSelectedImages().size());
                            }
                        })
                        .build());
            }
        }));

        mDataList.add(new H("类微信带拼音索引的联系人列表", new Runnable() {
            @Override
            public void run() {
                U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(TestSdkActivity.this, PickContactFragment.class)
                        .setHasAnimation(true)
                        .build());
            }
        }));

        mDataList.add(new H("二维码实验", new Runnable() {
            @Override
            public void run() {
                U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(TestSdkActivity.this, QrcodeTestFragment.class)
                        .setHasAnimation(true)
                        .build());
            }
        }));

        mDataList.add(new H("百度地图", new Runnable() {
            @Override
            public void run() {
                U.getLbsUtils().getLocation(true, new LbsUtils.Callback() {
                    @Override
                    public void onReceive(LbsUtils.Location location) {
                        U.getToastUtil().showShort(location.toString());
                        StatisticsAdapter.recordPropertyEvent(StatConstants.CATEGORY_USER_INFO, StatConstants.KEY_CITY, location.getCity());
                        StatisticsAdapter.recordPropertyEvent(StatConstants.CATEGORY_USER_INFO, StatConstants.KEY_DISTRICT, location.getDistrict());
                    }
                });
            }
        }));

        mDataList.add(new H("Rxretrofit实验", new Runnable() {
            @Override
            public void run() {
                U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(TestSdkActivity.this, RxRetrofitFragment.class)
                        .setHasAnimation(true)
                        .build());
            }
        }));

        mDataList.add(new H("崩溃收集", new Runnable() {
            @Override
            public void run() {
                try {
                    throw new IllegalStateException("测试，我是主动抛出的一个异常，使用 PgyCrashManager 上报");
                } catch (Exception e) {
                    PgyCrashManager.reportCaughtException(e);
                    U.getToastUtil().showShort("已上报一个自定义崩溃");
                }
            }
        }));

        mDataList.add(new H("用户反馈(Activity)", new Runnable() {
            @Override
            public void run() {
                new PgyerFeedbackManager.PgyerFeedbackBuilder()
                        .setShakeInvoke(true)           //设置是否摇一摇的方式激活反馈，默认为 true
                        .setBarBackgroundColor("")      // 设置顶部按钮和底部背景色，默认颜色为 #2E2D2D
                        .setBarButtonPressedColor("")        //设置顶部按钮和底部按钮按下时的反馈色 默认颜色为 #383737
                        .setColorPickerBackgroundColor("")   //设置颜色选择器的背景色,默认颜色为 #272828
                        .setBarImmersive(true)              //设置activity 是否以沉浸式的方式打开，默认为 false
                        .setDisplayType(PgyerFeedbackManager.TYPE.ACTIVITY_TYPE)   //设置以Dialog 的方式打开
                        .setMoreParam("渠道号", U.getChannelUtils().getChannel())
                        .setMoreParam("KEY2", "VALUE2")
                        .builder()
                        .invoke();                  //激活直接显示的方式

            }
        }));
        mDataList.add(new H("用户反馈(Dialog)", new Runnable() {
            @Override
            public void run() {
                new PgyerFeedbackManager.PgyerFeedbackBuilder()
                        .setShakeInvoke(true)       //设置是否摇一摇的方式激活反馈，默认为 true
//                        .setColorDialogTitle("")    //设置Dialog 标题栏的背景色，默认为颜色为#ffffff
//                        .setColorTitleBg("")        //设置Dialog 标题的字体颜色，默认为颜色为#2E2D2D
                        .setDisplayType(PgyerFeedbackManager.TYPE.DIALOG_TYPE)   //设置以Dialog 的方式打开
                        .setMoreParam("渠道号", U.getChannelUtils().getChannel())
                        .setMoreParam("KEY2", "VALUE2")
                        .builder()
                        .invoke();                  //激活直接显示的方式

            }
        }));
        mDataList.add(new H("激活摇一摇用户反馈(Dialog)", new Runnable() {
            @Override
            public void run() {
                new PgyerFeedbackManager.PgyerFeedbackBuilder()
                        .setShakeInvoke(true)       //设置是否摇一摇的方式激活反馈，默认为 true
//                        .setColorDialogTitle("")    //设置Dialog 标题栏的背景色，默认为颜色为#ffffff
//                        .setColorTitleBg("")        //设置Dialog 标题的字体颜色，默认为颜色为#2E2D2D
                        .setDisplayType(PgyerFeedbackManager.TYPE.DIALOG_TYPE)   //设置以Dialog 的方式打开
                        .setMoreParam("渠道号", U.getChannelUtils().getChannel())
                        .setMoreParam("KEY2", "VALUE2")
                        .builder()
                        .register();                //注册摇一摇的方式
                U.getToastUtil().showShort("注册成功，晃动手机可弹出反馈页面");

            }
        }));

//        mDataList.add(new H("Span 测试", new Runnable() {
//            @Override
//            public void run() {
//                U.getFragmentUtils().addFragment(FragmentUtils.newParamsBuilder(TestSdkActivity.this, QrcodeTestFragment.class)
//                        .setHasAnimation(true)
//                        .build());
//            }
//        }));

        mDataList.add(new H("手动触发小米统计上报", new Runnable() {
            @Override
            public void run() {
                MiStatInterface.setUploadPolicy(MiStatInterface.UPLOAD_POLICY_DEVELOPMENT, 0);
                MiStatInterface.triggerUploadManually();
            }
        }));

        mDataList.add(new H("日志全开", new Runnable() {
            @Override
            public void run() {
                MyLog.setLogcatTraceLevel(0);
            }
        }));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!U.getPermissionUtils().checkExternalStorage(this)) {
            U.getPermissionUtils().requestExternalStorage(new PermissionUtils.RequestPermission() {
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
        if (!U.getPermissionUtils().checkRecordAudio(this)) {
            U.getPermissionUtils().requestRecordAudio(new PermissionUtils.RequestPermission() {
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
        U.getToastUtil().showShort("网络变化 now:" + event.type);
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public boolean canSlide() {
        return false;
    }


}
