package com.wali.live.watchsdk.videodetail.data;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.engine.player.GalileoPlayer;
import com.mi.live.engine.streamer.GalileoStreamer;
import com.wali.live.dns.IDnsStatusListener;
import com.wali.live.dns.PreDnsManager;
import com.wali.live.ipselect.BaseIpSelectionHelper;
import com.wali.live.ipselect.FeedsIpSelectionHelper;
import com.wali.live.ipselect.WatchIpSelectionHelper;

import java.lang.ref.WeakReference;

/**
 * Created by yangli on 2017/3/15.
 * <p>
 * 推拉流模块采用外观(Facade)模式，内部由三个子部件组成：推/拉流器，重连器，域名解析器。<br/>
 * 子类通过范型指定参与工作的具体的子组件类型。比如，StreamerPresenter(推流)、PullStreamerPresenter(拉流)、
 * ThirdStreamerPresenter(提供给第三方播放器的拉流)
 * <p>
 * 1) <strong>推流逻辑</strong>
 * <ul>
 * 1.1) <strong>推流器</strong> {@link GalileoStreamer}
 * <p>
 * 推流器采用代理模式实现，封装引擎团队提供的推流库，并使用单线程串行化对引擎的操作。
 * 具体实现细节请自行查阅{@link GalileoStreamer}类的源码。
 * <p>
 * 1.2) <strong>推流域名解析器</strong> {@link MultiCdnIpSelectionHelper}
 * <p>
 * 推流域名解析器封装了推流时的域名解析和推流策略管理：
 * <p>
 * 开始推流时，会从服务器获取到多个地址，若存在UDP地址，则优先使用UDP地址推流，若UDP推流无法成功，才使用TCP地址推流。<br/>
 * 域名解析时，会为当前地址列表中的所有域名进行域名解析，获取到每个域名的IP地址列表。当调用{@link MultiCdnIpSelectionHelper#setOriginalStreamUrl}时，
 * 会自动为当前使用的地址列表的每个域名进行域名解析，若针对某个域名进行解析失败，则当下次重连调用{@link MultiCdnIpSelectionHelper#ipSelect}时，
 * 会检查是否存在某个域名未成功解析，若存在，则会重新解析该域名。
 * <p>
 * 显然，某次重连触发的域名解析操作获取到的IP列表，要等到下次重连时才会给到引擎。并且当所有域名都被成功解析后，
 * 重连操作只是让引擎使用现有地址和IP信息重新启动其底层的推流操作。域名解析操作是通过使用RxJava确保在异步线程串行化执行。
 * <p>
 * 首次域名解析成功时(即域名解析操作完成后，至少存在一个IP变得可用)，
 * 会触发一次{@link IDnsStatusListener#onDnsReady}回调，推流重连器会处理该回调。
 * <p>
 * 每次开始推流，不管IP解析是否全部成功，均将当前全部地址和IP信息给到引擎，具体使用哪个地址和IP，由引擎底层自己选择。
 * 这也是推流域名解析被称为MultiCdnIpSelectionHelper的原因。由于这是中途引入的修改，
 * 所以推拉流域名解析器没有抽取公共的轻量级基类。
 * <p>
 * 1.3) <strong>推流重连器</strong> {@link StreamerPresenter.ReconnectHelper}
 * 推流重连器封装了推流的重连逻辑，当首次开始推流时，会尝试启动推流操作，若域名解析器已准备就绪，则推流立马开始；
 * 否则，则会延迟5秒，等待{@link IDnsStatusListener#onDnsReady}回调，若5秒超时或onDnsReady回调被触发，
 * 则会强制启动推流操作。<br/>
 * 针对特定域名，推流重连器还负责执行降码率逻辑，最低降至0.6。具体参见{@link StreamerPresenter.ReconnectHelper#onDropRate}
 * </ul>
 * 2) <strong>拉流逻辑</strong>
 * <ul>
 * 2.1) <strong>拉流器</strong> {@link GalileoPlayer}
 * <p>
 * 拉流器采用代理模式实现，封装引擎团队提供的拉流库，并使用单线程串行化对引擎的操作。
 * 具体实现细节请自行查阅{@link GalileoPlayer}类的源码。
 * <p>
 * 2.2) <strong>拉流域名解析器</strong> {@link BaseIpSelectionHelper} {@link WatchIpSelectionHelper}
 * {@link FeedsIpSelectionHelper}
 * <p>
 * 拉流域名解析器封装了拉流时的域名解析和拉流策略管理：
 * <p>
 * 与推流不同，拉流只有一个地址，但拉流包含诸如拼接URL、设置端口信息等规则，且当网络不好时(比如，拉取了2次保底IP)，
 * 需要使用低码率地址进行拉流。同时{@link FeedsIpSelectionHelper}还需要处理本地URL的情况。
 * 此外，拉流本身还存在上下滑动时切换房间的场景。
 * <p>
 * 当调用{@link BaseIpSelectionHelper#setOriginalStreamUrl}时，会判断当前域名是否发生变化，若是，
 * 则会自动对变化后的域名进行解析。每次调用{@link BaseIpSelectionHelper#ipSelect}时，
 * 选取两个IP(若剩余不足两个则只选一个)，同时拼接一个新的URL，将URL和IP都给到引擎，
 * 具体使用哪个IP会受引擎底层跑马影响。
 * <p>
 * 拉流域名解析器拉取到的IP分为Local IP，Http IP和保底IP。Local IP和Http IP是重复使用的。
 * 每次调用{@link BaseIpSelectionHelper#ipSelect}时选取一个Local IP和一个Http IP，给到引擎进行推流；
 * 当这两类IP全部耗尽时，拉取一次保底IP，若拉取成功，则开始使用保底IP，并重置Local和Http的索引，当保底IP耗尽时，
 * 将会从头使用Local IP列表和Http IP列表，如此循环往复。
 * <p>
 * 拼接URL时，需要为IP设置端口信息，若选取了2个IP，比如 {IP1, IP2}，服务器为域名配置了2个端口，比如 {80, 8080}，
 * 则设置端口信息之后的IP数为2*2=4，即Ip与端口进行组合，{IP1:80, IP1:8080, IP2:80, IP2:8080}。
 * <p>
 * 域名解析操作是通过使用RxJava确保在异步线程串行化执行。
 * <p>
 * 首次域名解析成功时(即域名解析操作完成后，至少存在一个IP变得可用)，
 * 会触发一次{@link IDnsStatusListener#onDnsReady}回调，拉流重连器会处理该回调。
 * <p>
 * 2.3) <strong>拉流重连器</strong> {@link PullStreamerPresenter.ReconnectHelper}
 * <p>
 * 拉流重连器封装了拉流的重连逻辑，当首次开始拉流时，会直接启动拉流操作，
 * 当收到{@link IDnsStatusListener#onDnsReady}回调时，会检查当前是否卡顿，若是，则表明当前推流未成功，
 * 此时会触发一次重连操作，将域名替换成IP。
 * </ul>
 * 1) <strong>域名预解析</strong>
 * <ul>
 * 域名解析结果会缓存在名为{@link PreDnsManager}的单例中，以降低网络请求的频率。
 * PreDnsManager还负责对服务器下发的域名列表进行预解析，以加速推/拉流开始时的首次域名解析。同时，
 * 服务配置的域名对应的IP信息也保存在该类中。
 * </ul>
 *
 * @module 推拉流
 */
public abstract class BaseStreamerPresenter<R extends BaseStreamerPresenter.ReconnectHelper, IP, STREAM>
        extends RxLifeCyclePresenter {
    protected final String TAG = getTAG();

    protected static final int RECONNECT_TIMEOUT = 5 * 1000;    // 卡顿超时换IP时间
    protected static final int START_STREAM_TIMEOUT = 5 * 1000; // 推流超时时间

    protected static final int MSG_START_STREAM = 1001;         // 启动流
    protected static final int MSG_START_STREAM_TIMEOUT = 1002; // 推/拉流超时
    protected static final int MSG_START_STREAM_FAILED = 1003;  // 推/拉流失败

    protected MyUIHandler mUIHandler;
    protected R mReconnectHelper;
    protected IP mIpSelectionHelper;

    protected STREAM mStreamer;

    protected abstract String getTAG();

    public final void setStreamer(STREAM streamer) {
        mStreamer = streamer;
    }

    /**
     * 重连
     */
    protected abstract class ReconnectHelper {
        /**
         * 开始推/拉流
         */
        protected abstract void startStream();

        /**
         * 停止推/拉流
         */
        protected abstract void stopStream();

        /**
         * 开始重连
         */
        protected abstract void startReconnect(int code);

        public ReconnectHelper() {
        }
    }

    /**
     * 消息队列
     */
    protected static class MyUIHandler<T extends BaseStreamerPresenter> extends Handler {
        protected final WeakReference<T> mPresenterRef;
        protected final String TAG;

        protected final <T> T deRef(WeakReference<T> reference) {
            return reference != null ? reference.get() : null;
        }

        public MyUIHandler(@NonNull T presenter) {
            mPresenterRef = new WeakReference<>(presenter);
            TAG = presenter.getTAG();
        }
    }
}
