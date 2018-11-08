公共基础库


1.那如果有多个Disposable 该怎么办呢, RxJava中已经内置了一个容器CompositeDisposable,
每当我们得到一个Disposable时就调用CompositeDisposable.add()将它添加到容器中,
在退出的时候, 调用CompositeDisposable.clear() 即可切断所有的水管.

// rxjava2 解决 背压 的方式

  Flowable.create(new FlowableOnSubscribe<Object>() {
                @Override
                public void subscribe(FlowableEmitter<Object> emitter) throws Exception {

                }
            }, BackpressureStrategy.DROP)
                    .subscribe(new Subscriber<Object>() {
                        @Override
                        public void onSubscribe(Subscription s) {
                            // 叶问打鬼子，我要打10个
                            // 下游描述自己的处理能力
                            s.request(10);
                        }

                        @Override
                        public void onNext(Object o) {

                        }

                        @Override
                        public void onError(Throwable t) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });


debounce与throttle的原理 ,内部是一个不断循环的轮询，知道满足条件才中止

Stetho 调试

2.增加数据库升级管理类 完成
3.增加左滑退出组件 完成
4.增加数据库调试工具 完成
5.下载上传公共类 完成
6. svg 生成矢量图
7.

shape标签
selector标签
level-list标签
transition标签
rotate标签
animation-list标签
animated-rotate标签

模版代码
<pre>
    <?xml version="1.0" encoding="utf-8"?>
    <selector xmlns:android="http://schemas.android.com/apk/res/android">
        <item android:state_pressed="false">
            <shape android:shape="rectangle">
                <!-- 填充的颜色 -->
                <solid android:color="@android:color/white"></solid>
                <!-- android:radius 弧形的半径 -->
                <corners android:radius="6px" />
                <!-- 绘制边框-->
                <stroke android:width="2px" android:color="#2c72ac" android:dashGap="0dp" />
            </shape>
        </item>
        <item android:state_pressed="true">
            <shape android:shape="rectangle">
                <solid android:color="@android:color/white" />
                <corners android:radius="6px" />
                <stroke android:width="2px" android:color="#3c99e5" android:dashGap="0dp" />
            </shape>
        </item>

        <item android:color="@color/blue_3C99E5" android:state_focused="true"></item>
        <item android:color="@color/blue_3C99E5" android:state_pressed="true"></item>
        <item android:color="@color/blue_CCCCCC"></item>

    </selector>
</pre>

