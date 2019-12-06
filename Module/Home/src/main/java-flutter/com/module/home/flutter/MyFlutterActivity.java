package com.module.home.flutter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.module.RouterConstants;
import com.module.home.R;

import io.flutter.facade.Flutter;
import io.flutter.view.FlutterView;

/**
 * TODO
 * 这个容器应该当作基础组件放在 commonsdk 里 ，再考虑下
 */
@Route(path = RouterConstants.ACTIVITY_FLUTTER)
public class MyFlutterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flutter);

        final FlutterView flutterView = Flutter.createView(
                this,
                getLifecycle(),
                "route1"
        );


        final FrameLayout layout = findViewById(R.id.flutter_container);
        layout.addView(flutterView);

        final FlutterView.FirstFrameListener[] listeners = new FlutterView.FirstFrameListener[1];
        listeners[0] = new FlutterView.FirstFrameListener() {
            @Override
            public void onFirstFrame() {
                layout.setVisibility(View.VISIBLE);
            }
        };
        flutterView.addFirstFrameListener(listeners[0]);

    }
}
