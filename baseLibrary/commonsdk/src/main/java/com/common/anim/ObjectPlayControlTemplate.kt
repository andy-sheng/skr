package com.common.anim

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.v4.util.Pair

import com.common.log.MyLog
import com.common.utils.CustomHandlerThread

import java.util.LinkedList

/**
 * 循环播放动画时的控制模板类，封装点亮、背景、大礼物等播放的基本队列逻辑
 * Created by chengsimin on 16/6/17.
 */
abstract class ObjectPlayControlTemplate<MODEL, CONSUMER> {

    /**
     * 播放动画队列
     */
    private val mQueue = LinkedList<MODEL>()

    internal var mHandlerThread: CustomHandlerThread// 保证++ --  都在后台线程操作

    internal val mUiHandler: Handler

    val size: Int
        get() = mQueue.size

    init {
        mHandlerThread = object : CustomHandlerThread("ObjectPlayControlTemplate") {
            override fun processMessage(var1: Message) {

            }
        }
        mUiHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    MSG_START_ON_UI -> {
                        val pair = msg.obj as Pair<MODEL, CONSUMER>
                        onStart(pair.first!!, pair.second!!)
                    }
                    MSG_END_ON_UI -> onEnd(msg.obj as MODEL?)
                    MSG_ACCEPT_ON_UI -> {
                        val cur = msg.obj as MODEL
                        val consumer = accept(cur)
                        mHandlerThread.post {
                            if (consumer != null) {
                                // 肯定有消费者，才会走到这
                                val cur = mQueue.poll()
                                if (cur != null) {
                                    //取出来一个
                                    processInBackGround(cur)
                                    onStartInside(cur, consumer)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun add(model: MODEL, must: Boolean) {
        mHandlerThread.post {
            if (mQueue.size < SIZE || must) {
                mQueue.offer(model)
            }
            play()
        }
    }

    private fun play() {
        val cur = mQueue.peekFirst()
        if (cur != null) {
            val msg = mUiHandler.obtainMessage(MSG_ACCEPT_ON_UI)
            msg.obj = cur
            mUiHandler.sendMessage(msg)
            //            CONSUMER consumer = accept(cur);
            //            if (consumer != null) {
            //                // 肯定有消费者，才会走到这
            //                cur = mQueue.poll();
            //                if (cur != null) {
            //                    //取出来一个
            //                    processInBackGround(cur);
            //                    onStartInside(cur, consumer);
            //                }
            //            }
        }

    }

    /**
     *
     * @param model
     */
    private fun onStartInside(model: MODEL, consumer: CONSUMER) {
        MyLog.d(MODELAG, "onStartInside model:" + model!!)
        val msg = mUiHandler.obtainMessage(MSG_START_ON_UI)
        msg.obj = Pair(model, consumer)
        mUiHandler.sendMessage(msg)
    }

    /**
     * 重要，每次消费完，请手动调用告知
     * 这样模型才继续前进 取 下一个消费对象
     *
     * @param model
     */
    fun endCurrent(model: MODEL?) {
        val msg = mUiHandler.obtainMessage(MSG_END_ON_UI)
        msg.obj = model
        mUiHandler.sendMessage(msg)

        mHandlerThread.post { onEndInSide(model) }
    }

    private fun onEndInSide(model: MODEL?) {
        MyLog.d(MODELAG, "onEndInSide model:$model")
        play()
    }

    /**
     * 复位
     */
    @Synchronized
    fun reset() {
        mQueue.clear()
    }

    /**
     * 复位
     */
    @Synchronized
    fun destroy() {
        mQueue.clear()
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null)
        }
        mHandlerThread.destroy()
    }


    /**
     * 是否接受这个播放对象
     * 如果不接受 不会从队列移除被消费
     * @param cur
     * @return
     */
    protected abstract fun accept(cur: MODEL): CONSUMER?

    /**
     * 某次动画开始时执行
     *
     * @param model
     */
    abstract fun onStart(model: MODEL, consumer: CONSUMER)

    /**
     * 某次动画结束了执行
     *
     * @param model
     */
    protected abstract fun onEnd(model: MODEL?)

    protected fun processInBackGround(model: MODEL?) {

    }

    /**
     * 队列这是否还有
     *
     * @return
     */
    @Synchronized
    fun hasMoreData(): Boolean {
        return !mQueue.isEmpty()
    }

    companion object {
        val MODELAG = "AnimationPlayControlMODELemplate"

        internal val MSG_START_ON_UI = 80

        internal val MSG_END_ON_UI = 81

        internal val MSG_ACCEPT_ON_UI = 82

        val SIZE = 100//生产者池子里最多多少个
    }


}
