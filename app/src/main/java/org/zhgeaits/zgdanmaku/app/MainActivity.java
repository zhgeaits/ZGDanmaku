/*
 * Copyright (C) 2016 Zhang Ge <zhgeaits@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zhgeaits.zgdanmaku.app;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.zhgeaits.zgdanmaku.ZGDanmakuView;

public class MainActivity extends Activity {

    private ZGDanmakuView danmakuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置为全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置为竖屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);

        danmakuView = (ZGDanmakuView) findViewById(R.id.danmaku);
        danmakuView.setSpeed(150);
        danmakuView.setLines(10);
        danmakuView.setLineSpace(2);

        Button closeSwitcher = (Button) findViewById(R.id.openOrClose);
        closeSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                danmakuView.setOpen(!danmakuView.isOpen());
            }
        });

        Button pauseSwitcher = (Button) findViewById(R.id.pauseOrResume);
        pauseSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                danmakuView.setPaused(!danmakuView.isPaused());
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (int i = 0; i < 10; i ++) {
                        danmakuView.shotDanmamku("hello world!");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        danmakuView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        danmakuView.onPause();
    }
}
