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

import org.zhgeaits.zgdanmaku.view.ZGDanmakuTextureView;
import org.zhgeaits.zgdanmaku.view.ZGDanmakuView;

public class MainActivity extends Activity {

    private ZGDanmakuTextureView danmakuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置为全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置为竖屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);

        danmakuView = (ZGDanmakuTextureView) findViewById(R.id.danmaku);
        danmakuView.setSpeed(150);
        danmakuView.setLines(10);
        danmakuView.setLeading(2);
        danmakuView.start();

        Button closeSwitcher = (Button) findViewById(R.id.openOrClose);
        closeSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(danmakuView.isHide()) {
                    danmakuView.show();
                } else {
                    danmakuView.hide();
                }
            }
        });

        Button pauseSwitcher = (Button) findViewById(R.id.pauseOrResume);
        pauseSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(danmakuView.isPause()) {
                    danmakuView.resume();
                } else {
                    danmakuView.pause();
                }
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (int i = 0; i < 100; i ++) {
                        danmakuView.shotTextDanmamku("hello world!");
                    }
                    try {
                        Thread.sleep(10000);
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
