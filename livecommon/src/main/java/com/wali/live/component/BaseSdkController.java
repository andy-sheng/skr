package com.wali.live.component;

import com.thornbirds.component.ComponentController;
import com.thornbirds.component.ComponentView;
import com.thornbirds.component.CompoundView;
import com.thornbirds.component.IParams;
import com.thornbirds.component.Params;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.thornbirds.component.presenter.EventPresenter;
import com.thornbirds.component.view.IComponentView;
import com.thornbirds.component.view.IEventView;

/**
 * Created by yangli on 2017/8/2.
 * <p>
 * <b>Component</b>是基于总线结构实现的UI消息架构。
 * 针对直播内的复杂场景而设计，将页面中逻辑上归属一类的View抽象为组件(比如，顶部区域、底部区域、悬浮面板区域、挂件等)，
 * 每个组件采用MVP模式实现，View通过Presenter进行数据刷新以及与其他组件通信。
 * 一般而言，组件应在其Presenter中实现自身所需的所有逻辑，只在必要时才与其他组件通信。<br/>
 * <b>注意</b>：目前的实现中，事件发送只能在主线程中进行，并直接以block方式调用监听者的onEvent回调。
 * 因此过于频繁发生的事件，请考虑其他方式。
 * <p>
 * 基础组成如下：
 * <p>
 * 1) <strong>消息总线</strong> {@link ComponentController}
 * <ul>
 * <li>提供<em><b>[un]registerObserver*</b></em>操作[取消]注册对某类消息的监听。</li>
 * <li>提供<em><b>postEvent</b></em>操作向总线发送某类消息。</li>
 * <li>提供<em><b>release</b></em>操作销毁总线并释放资源。</li>
 * </ul>
 * 一般通过继承ComponentController自定义总线，通常会包含该总线支持的所有消息类型的定义(整型常量)。
 * Controller同时也被用作控制器，可用于承载页面的控制逻辑(比如Activity、Fragment，当页面逻辑比较复杂时才推荐使用Component架构)，
 * 尤其当页面存在差距很大的不同状态时，Controller可用于存放跨页面的逻辑。比如，详情播放页中，
 * 半屏和全屏播放分别由两个页面承载，播放器由Controller管理，当进行大小屏切换时，播放器不需要被销毁重建。
 * <p>
 * 2) <strong>页面视图</strong> {@link ComponentView}，继承自 {@link CompoundView}
 * <p>
 * <b>辅助函数</b>:
 * <ul>
 * <li>提供<em><b>registerComponent</b></em>用于设置组件的View和Presenter，使其相互引用，以便能协同工作；
 * 注册之后，该组件生命周期将由页面视图管理。</li>
 * <li>提供<em><b>registerHybridComponent</b></em>与registerComponent一样，只是此时View为普通安卓View，
 * 适用于只需单向通过Presenter操作View的场景。</li>
 * </ul>
 * <b>生命周期函数</b>:
 * <ul>
 * <li>提供<em><b>setupView</b></em>用于初始化页面视图。<br/>
 * 在实现该函数时，需要创建或找到页面视图的根View，并找到其子View中组件级的View，实例化Presenter，
 * 然后调用registerComponent或registerHybridComponent进行注册。</li>
 * <li>提供<em><b>startView</b></em>用于启动页面视图。<br/>
 * 调用该函数后，页面视图开始工作，组件实现者应确保该操作之后，组件能通过总线接到自己感兴趣的消息。
 * 基类的默认实现会调用setupView中注册的所有组件的Presenter的startPresenter操作。一般在start/startPresenter操作中，
 * 组件可以通过registerObserver*操作向总线注册自己感兴趣的事件，这样当事件发生时，总线便会向它发送通知。
 * 如果需要使用EventBus，也请在该函数中注册。</li>
 * <li>提供<em><b>stopView</b></em>用于停止页面视图。<br/>
 * 调用该函数后，页面视图停止工作，组件实现者应确保该操作之后，总线不再向组件发送其感兴趣的消息。
 * 如需重新开始工作，再次调用startView即可。基类的默认实现会调用setupView中注册的所有组件的Presenter的stopPresenter操作。
 * 一般在stop/stopPresenter操作中，若组件在start/startPresenter中向总线注册过消息监听，则此时应调用unregisterObserver
 * 操作取消监听。如果注册了EventBus，也请在该函数中取消注册。</li>
 * <li>提供<em><b>release</b></em>用于释放页面视图。<br/>
 * 基类的默认实现会调用setupView中注册的所有组件的Presenter的destroy操作。一般在release/destroy操作中，
 * 你必须释放占用的资源</li>
 * </ul>
 * setupView、startView、stopView、release提供了管理页面视图生命周期的全部操作。如果在继承时想要拓展这些操作，
 * 请不要忘记调用super中的实现。<b>注意</b>：由页面视图直接管理的组件，其存活周期与该页面视图相同。
 * 如果你想用弱引用或者软引用，优化内存，可以参考PanelContainerPresenter的实现。但是，由页面视图直接管理的组件，
 * 不推荐做这种优化。
 * <p>
 * 3) <strong>组件实现</strong>
 * <p>
 * 3.1) <strong>组件表现接口</strong> {@link ComponentPresenter}，继承自{@link EventPresenter}
 * <p>
 * 你必须通过继承ComponentPresenter实现自己的Presenter。并且可以通过范型指定与之交互的VIEW的具体类型。
 * 一般将VIEW指定为具体View实现了的某个接口。
 * <p>
 * <b>辅助函数</b>:
 * <ul>
 * <li>提供<em><b>[un]registerAction</b></em>操作向总线[取消]注册本组件对某类消息的监听。<br/>
 * 如果想取消注册本组件之前注册过的所有事件监听，可使用<em><b>unregisterAllAction</b></em>。</li>
 * <li>提供<em><b>postEvent</b></em>操作向总线发送某类消息。<br/>
 * 当本组件的某些改变会引起其他改变时，通过发送事件通知其他组件，接收事件的组件应该接收事件并进行相应处理。
 * 默认发送事件时不携带参数，若需要携带参数，可实例一个{@link Params}对象，通过putItem按顺序放入参数。
 * 接收方通过getItem获取参数。发送方和接收方需要约定参数类型和存放顺序。<br/>
 * Params没有使用对象池进行优化，后续可考虑加入此优化。也可以通过拓展{@link IParams}，实现自定义的参数传递逻辑。</li>
 * </ul>
 * <b>生命周期函数</b>:
 * <ul>
 * <li>提供<em><b>startPresenter</b></em>操作启动Presenter。<br/>
 * 若组件在启动时，需要需要执行额外的启动逻辑，你应该重写该方法，并实现这些操作。例如，向总线注册事件监听、注册EventBus
 * 或者注册广播等。</li>
 * <li>提供<em><b>stopPresenter</b></em>操作停止Presenter。<br/>
 * 若组件在停止时，需要需要执行额外的启动逻辑，你应该重写该方法，并实现这些操作。例如，向总线取消注册事件监听、
 * 取消注册EventBus或者取消注册广播等。</li>
 * <li>提供<em><b>destroy</b></em>操作销毁Presenter并释放资源。<br/>
 * 一般建议在stopPresenter操作中释放资源，当你希望使用弱引用或者软引用优化该组件时，这将很有用。只要组件被停止，
 * JVM便能在内存紧张时将其回收，防止不必要的内存占用。但是，当这种优化不必要时，比如组件实例化的成本很高时，你应该在
 * 构造函数中申请资源，在该函数中释放资源。</li>
 * </ul>
 * 3.2) <strong>组件视图接口</strong> {@link IComponentView}，继承自{@link IEventView}
 * <p>
 * 定义了组件View应该实现的接口。当组件View和Presenter需要相互操作时，让具体View实现该接口即可。
 * 可以通过范型指定与View交互的Presenter的具体类型，以及View暴露给Presenter的接口。<br/>
 * 在我们构建的框架中，IView和IPresenter接口，都是以自定义View的内部接口形式声明的，因此View本身无法直接实现该接口，
 * 只能通过getViewProxy操作，返回一个实现了IView接口的代理类，Presenter通过这个代理类与View交互。
 * <p>
 * View和Presenter的交互的具体实现，可参考DetailCommentView与DetailCommentPresenter、DetailInfoView与DetailInfoPresenter等。
 * 这只是架构作者给出的推荐实现，这是一种编程实践。若你有更好的实现方式，可以对其进行改进，比如使用MVVM等。
 * <p>
 * 4) <strong>Python脚本</strong>
 * <p>
 * 在工程的python目录下，我们提供了脚本，用于辅助创建View和[或]Presenter。
 * 如果是第一次运行，需要为脚本配置Python环境，执行sh setup_python.sh即可。然后通过cd命令，切换到相应模块路径中，
 * 执行<br/><em>python -m create_view_with_presenter -n Test</em><br/>
 * 脚本会自动生成TestView.java和TestPresenter.java文件。
 * <p>
 * 其他使用用法，请使用<em>python -m create_view_with_presenter -h </em>查看。
 * <p>
 * <a href="https://github.com/ModerateFish/component">GitHub源码</a>
 *
 * @module 基础架构控制器
 */
public abstract class BaseSdkController extends ComponentController {

    // 系统消息
    private static final int MSG_SYSTEM_FIRST = 10000;
    public static final int MSG_ON_BACK_PRESSED = MSG_SYSTEM_FIRST;         // 返回键
    public static final int MSG_ON_ORIENT_PORTRAIT = MSG_SYSTEM_FIRST + 1;  // 竖屏
    public static final int MSG_ON_ORIENT_LANDSCAPE = MSG_SYSTEM_FIRST + 2; // 横屏
    public static final int MSG_ON_ACTIVITY_RESUMED = MSG_SYSTEM_FIRST + 3; // APP回到前台

    // 推/拉流相关消息
    private static final int MSG_STREAM_FIRST = 11000;
    public static final int MSG_END_LIVE_UNEXPECTED = MSG_STREAM_FIRST;      // 异常结束直播/观看
    public static final int MSG_END_LIVE_FOR_TIMEOUT = MSG_STREAM_FIRST + 1; // 长时间退到后台后结束直播
    public static final int MSG_OPEN_CAMERA_FAILED = MSG_STREAM_FIRST + 2;   // 打开相机失败
    public static final int MSG_OPEN_MIC_FAILED = MSG_STREAM_FIRST + 3;      // 打开麦克风失败
    public static final int MSG_ON_STREAM_SUCCESS = MSG_STREAM_FIRST + 4;    // 推/拉流成功
    public static final int MSG_ON_STREAM_RECONNECT = MSG_STREAM_FIRST + 5;  // 开始重连
    public static final int MSG_ON_LIVE_SUCCESS = MSG_STREAM_FIRST + 6;      // 开房间/进房间成功
    public static final int MSG_NEW_VIDEO_URL = MSG_STREAM_FIRST + 7;        // 获取到拉流URL, 可以开始观看了

    // 播放器相关消息
    private static final int MSG_PLAYER_FIRST = 12000;
    public static final int MSG_VIDEO_SIZE_CHANGED = MSG_PLAYER_FIRST;       // 视频尺寸更新
    public static final int MSG_PLAYER_READY = MSG_PLAYER_FIRST + 1;         // 播放器开始渲染画面
    public static final int MSG_PLAYER_ERROR = MSG_PLAYER_FIRST + 2;         // 播放器出错
    public static final int MSG_PLAYER_COMPLETED = MSG_PLAYER_FIRST + 3;     // 播放完成
    public static final int MSG_SEEK_COMPLETED = MSG_PLAYER_FIRST + 4;       // Seek完成
    public static final int MSG_UPDATE_PLAY_PROGRESS = MSG_PLAYER_FIRST + 5; // 更新播放进度
    public static final int MSG_PLAYER_SHOW_LOADING = MSG_PLAYER_FIRST + 6;  // 显示 加载图标
    public static final int MSG_PLAYER_HIDE_LOADING = MSG_PLAYER_FIRST + 7;  // 隐藏 加载图标
    //    public static final int MSG_PLAYER_START = MSG_PLAYER_FIRST + 8;
    public static final int MSG_PLAYER_PAUSE = MSG_PLAYER_FIRST + 9;

    // UI消息
    // 复合消息(多个View同时响应的消息)
    private static final int MSG_COMPOUND_FIRST = 20000;
    public static final int MSG_INPUT_VIEW_SHOWED = MSG_COMPOUND_FIRST;       // 输入框 已显示
    public static final int MSG_INPUT_VIEW_HIDDEN = MSG_COMPOUND_FIRST + 1;   // 输入框 已隐藏
    public static final int MSG_BOTTOM_POPUP_SHOWED = MSG_COMPOUND_FIRST + 2; // 底部面板/礼物页面等显示时，通知 底部按钮和弹幕区 隐藏
    public static final int MSG_BOTTOM_POPUP_HIDDEN = MSG_COMPOUND_FIRST + 3; // 底部面板/礼物页面等隐藏时，通知 底部按钮和弹幕区 显示
    public static final int MSG_FORCE_ROTATE_SCREEN = MSG_COMPOUND_FIRST + 4; // 强制旋转UI
    public static final int MSG_VIDEO_PORTRAIT = MSG_COMPOUND_FIRST + 5;      // 视频流方向为竖向
    public static final int MSG_VIDEO_LANDSCAPE = MSG_COMPOUND_FIRST + 6;     // 视屏流方向为横向

    // 触摸相关消息
    private static final int MSG_TOUCH_FIRST = 21000;
    public static final int MSG_ENABLE_MOVE_VIEW = MSG_TOUCH_FIRST;      // 开启滑动
    public static final int MSG_DISABLE_MOVE_VIEW = MSG_TOUCH_FIRST + 1; // 禁止滑动
    public static final int MSG_BACKGROUND_CLICK = MSG_TOUCH_FIRST + 2;  // 背景点击
    public static final int MSG_PAGE_DOWN = MSG_TOUCH_FIRST + 3;         // 下滑点击
    public static final int MSG_PAGE_UP = MSG_TOUCH_FIRST + 4;           // 上滑点击
    public static final int MSG_SWITCH_ROOM = MSG_TOUCH_FIRST + 5;       // 切换房间

    // 输入框相关消息
    private static final int MSG_INPUT_FIRST = 22000;
    public static final int MSG_SHOW_INPUT_VIEW = MSG_INPUT_FIRST;         // 请求弹起 输入框
    public static final int MSG_HIDE_INPUT_VIEW = MSG_INPUT_FIRST + 1;     // 请求隐藏 输入框
    public static final int MSG_SHOW_GAME_INPUT = MSG_INPUT_FIRST + 2;     // 请求显示 游戏输入框
    public static final int MSG_HIDE_GAME_INPUT = MSG_INPUT_FIRST + 3;     // 请求隐藏 游戏输入框
    public static final int MSG_SHOW_GAME_BARRAGE = MSG_INPUT_FIRST + 4;   // 显示 游戏弹幕
    public static final int MSG_HIDE_GAME_BARRAGE = MSG_INPUT_FIRST + 5;   // 隐藏 游戏弹幕
    public static final int MSG_BARRAGE_FANS = MSG_INPUT_FIRST + 6;        // 显示隐藏 飘屏弹幕开关
    public static final int MSG_BARRAGE_ADMIN = MSG_INPUT_FIRST + 7;       // 显示隐藏 飘屏弹幕开关
    public static final int MSG_BARRAGE_VIP = MSG_INPUT_FIRST + 8;         // 显示隐藏 飘屏弹幕开关

    // 弹出页面相关消息
    private static final int MSG_POPUP_FIRST = 23000;
    public static final int MSG_SHOW_SETTING_PANEL = MSG_POPUP_FIRST;       // 显示 设置面板
    public static final int MSG_SHOW_MAGIC_PANEL = MSG_POPUP_FIRST + 1;     // 显示 美妆面板
    public static final int MSG_SHOW_PLUS_PANEL = MSG_POPUP_FIRST + 2;      // 显示 直播加面板
    public static final int MSG_SHOW_GIFT_PANEL = MSG_POPUP_FIRST + 3;      // 显示 礼物面板
    public static final int MSG_HIDE_BOTTOM_PANEL = MSG_POPUP_FIRST + 4;    // 隐藏 底部面板
    public static final int MSG_SHOW_ATMOSPHERE_VIEW = MSG_POPUP_FIRST + 5; // 显示 氛围面板
    public static final int MSG_SHOE_GAME_ICON = MSG_POPUP_FIRST + 6;       // 展示 游戏中心Icon
    public static final int MSG_SHOW_GAME_DOWNLOAD = MSG_POPUP_FIRST + 7;   // 展示 游戏中心下载框
    public static final int MSG_SHOW_SHARE_PANEL = MSG_POPUP_FIRST + 8;     // 显示 分享面板
    public static final int MSG_SHOW_PERSONAL_INFO = MSG_POPUP_FIRST + 9;   // 显示 个人信息页
    public static final int MSG_SHOW_FOLLOW_GUIDE = MSG_POPUP_FIRST + 10;   // 显示 游戏引导页面
    public static final int MSG_FOLLOW_COUNT_DOWN = MSG_POPUP_FIRST + 11;   // 显示 游戏引导页面之前的倒计时
    public static final int MSG_SHOW_SEND_ENVELOPE = MSG_POPUP_FIRST + 12;  // 显示 发送红包页面
    public static final int MSG_SHOW_MESSAGE_PANEL = MSG_POPUP_FIRST + 13;  // 显示 私信面板
    public static final int MSG_SHOW_MENU_PANEL = MSG_POPUP_FIRST + 14;     // 显示 更多面板
    public static final int MSG_ON_MENU_PANEL_HIDDEN = MSG_POPUP_FIRST + 15;// 更多面板已隐藏
    public static final int MSG_SHOW_FANS_PANEL = MSG_POPUP_FIRST + 16;     // 显示 更多面板

    //连麦/PK相关消息
    private static final int MSG_TOP_VIEW_FIRST = 24000;
    public static final int MSG_ON_LINK_MIC_START = MSG_TOP_VIEW_FIRST + 1; // 连麦 开始
    public static final int MSG_ON_LINK_MIC_STOP = MSG_TOP_VIEW_FIRST + 2;  // 连麦 结束
    public static final int MSG_ON_PK_START = MSG_TOP_VIEW_FIRST + 3;       // PK 开始
    public static final int MSG_ON_PK_STOP = MSG_TOP_VIEW_FIRST + 4;        // PK 结束

    // 详情播放相关
    private static final int MSG_DETAIL_VIDEO_FIRST = 30000;
    public static final int MSG_NEW_FEED_ID = MSG_DETAIL_VIDEO_FIRST;               // 新的Feed ID
    public static final int MSG_NEW_FEED_URL = MSG_DETAIL_VIDEO_FIRST + 1;          // 获取到URL, 可以开始播放
    public static final int MSG_SWITCH_TO_REPLAY_MODE = MSG_DETAIL_VIDEO_FIRST + 2; // 切换到回放模式(全屏播放)
    public static final int MSG_SWITCH_TO_DETAIL_MODE = MSG_DETAIL_VIDEO_FIRST + 3; // 切换到详情模式(半屏播放)
    public static final int MSG_UPDATE_LIKE_STATUS = MSG_DETAIL_VIDEO_FIRST + 4;    // 更新 点赞状态
    public static final int MSG_UPDATE_COMMENT_CNT = MSG_DETAIL_VIDEO_FIRST + 5;    // 更新 评论总数
    public static final int MSG_REPLAY_TOTAL_CNT = MSG_DETAIL_VIDEO_FIRST + 6;      // 更新 回放总数
    public static final int MSG_SHOW_COMMENT_INPUT = MSG_DETAIL_VIDEO_FIRST + 7;    // 回复 评论
    public static final int MSG_SEND_COMMENT = MSG_DETAIL_VIDEO_FIRST + 8;          // 发送 评论
    public static final int MSG_FOLD_INFO_AREA = MSG_DETAIL_VIDEO_FIRST + 9;        // 收起 信息区
    public static final int MSG_COMPLETE_USER_INFO = MSG_DETAIL_VIDEO_FIRST + 10;   // 点击回放每一条
    public static final int MSG_UPDATE_START_TIME = MSG_DETAIL_VIDEO_FIRST + 11;    // 更新 回放的录制时间(用于拉取房间消息/弹幕)
    public static final int MSG_UPDATE_TAB_TYPE = MSG_DETAIL_VIDEO_FIRST + 12;      // 更新 TAB数据(回放显示"评论"和"回放"，详情显示"详情"和评论)

    public static final int MSG_NEW_DETAIL_REPLAY = MSG_DETAIL_VIDEO_FIRST + 13;
    public static final int MSG_PLAYER_START = MSG_DETAIL_VIDEO_FIRST + 14;

    // 其他
    private static final int MSG_MORE_FIRST = 90000;
}
