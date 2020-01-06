package com.component.busilib.view

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.MotionEvent
import com.common.log.MyLog
import com.common.utils.dp
import java.lang.ref.WeakReference

class AutoPollRecyclerView : RecyclerView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    var autoPollTask: AutoPollTask? = null
    private val TIME_AUTO_POLL: Long = 16
    private var running: Boolean = false //表示是否正在自动轮询
    private var canRun: Boolean = false//表示是否可以自动轮询
    private var time = 0  // 滚动次数
    var itemHight = 0
    

    init {
        autoPollTask = AutoPollTask(this)
    }


    inner class AutoPollTask
    (reference: AutoPollRecyclerView) : Runnable {
        //使用弱引用持有外部类引用->防止内存泄漏
        private val mReference: WeakReference<AutoPollRecyclerView> = WeakReference<AutoPollRecyclerView>(reference)

        override fun run() {
            val recyclerView = mReference.get()
            if (recyclerView != null && recyclerView.running && recyclerView.canRun) {
                recyclerView.scrollBy(0, 2)
                recyclerView.time = recyclerView.time + 1

                if (recyclerView.time * 2 % itemHight == 0) {
                    recyclerView.time = 0
                    recyclerView.postDelayed(recyclerView.autoPollTask, 1000 + TIME_AUTO_POLL)
                } else {
                    if (recyclerView.time * 2 % itemHight < 2) {
                        recyclerView.postDelayed(recyclerView.autoPollTask, 1000 + TIME_AUTO_POLL)
                    } else {
                        recyclerView.postDelayed(recyclerView.autoPollTask, TIME_AUTO_POLL)
                    }
                }
            }
        }
    }

    //开启:如果正在运行,先停止->再开启
    fun start() {
        if (running)
            stop()
        canRun = true
        running = true
        time = 0
        postDelayed(autoPollTask, TIME_AUTO_POLL)
    }

    fun stop() {
        running = false
        removeCallbacks(autoPollTask)
    }
}