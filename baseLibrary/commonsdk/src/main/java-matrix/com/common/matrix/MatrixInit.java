package com.common.matrix;

import android.content.Intent;

import com.common.matrix.display.IssuesListActivity;
import com.common.utils.U;
import com.tencent.matrix.Matrix;
import com.tencent.matrix.iocanary.IOCanaryPlugin;
import com.tencent.matrix.iocanary.config.IOConfig;

public class MatrixInit {

    public static void init(){
        Matrix.Builder builder = new Matrix.Builder(U.app()); // build matrix
        builder.patchListener(new ReportPluginListener(U.app())); // add general pluginListener

        DynamicConfigImpl dynamicConfig = new DynamicConfigImpl(); // dynamic config


        /**
         * tag: io
         *
         * type，耗时这边的类型有两种 a. MAIN_THREAD_IO=1, 在主线程IO超过200ms b. BUFFER_TOO_SMALL=2, 重复读取同一个文件,同一个堆栈超过3次 c. REPEAT_IO=3, 读写文件的buffer过小，即小于4k d. CLOSE_LEAK=4, 文件泄漏
         *
         * path: 文件的路径
         *
         * size: 文件的大小
         *
         * cost: 读写的耗时
         *
         * stack: 读写的堆栈
         *
         * op: 读写的次数
         *
         * buffer: 读写所用的buffer大小，要求大于4k
         *
         * thread: 线程名
         *
         * opType: 1为读，2为写
         *
         * opSize: 读写的总大小
         *
         * repeat:
         *
         * a. REPEAT_IO : 重复的次数
         *
         * b. Main_IO：1 - 单次操作 2 - 连续读写 3 -2种行为
         */
        IOCanaryPlugin ioCanaryPlugin = new IOCanaryPlugin(new IOConfig.Builder()
                .dynamicConfig(dynamicConfig)
                .build());
        //add to matrix
        builder.plugin(ioCanaryPlugin);

        //init matrix
        Matrix.init(builder.build());
        // start plugin
        ioCanaryPlugin.start();
    }

    public static boolean isOpen() {
        return true;
    }

    public static void goIssueList() {
        U.app().startActivity(new Intent(U.app(), IssuesListActivity.class));
    }
}
