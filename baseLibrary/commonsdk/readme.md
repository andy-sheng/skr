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

2.增加数据库升级管理类
3.增加左滑退出组件
4.增加数据库调试工具
5.下载上传公共类