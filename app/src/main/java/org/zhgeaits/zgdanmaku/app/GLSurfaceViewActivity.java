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
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.zhgeaits.zgdanmaku.utils.TexturePool;
import org.zhgeaits.zgdanmaku.view.IZGDanmakuView;

public class GLSurfaceViewActivity extends Activity {

    private IZGDanmakuView danmakuView;
    public static Context gContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gContext = this;

        //设置为全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置为竖屏模式
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_glsurfaceview);

        TexturePool.uninit();
        danmakuView = (IZGDanmakuView) findViewById(R.id.danmaku);
        danmakuView.setSpeed(100);
        danmakuView.setLines(15);
        danmakuView.setLeading(2);

        Button startSwitcher = (Button) findViewById(R.id.StartOrStop);
        startSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!danmakuView.isStarted()) {
                    danmakuView.start();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < 10; i ++) {
                                danmakuView.shotTextDanmakuAt("I am 3!", 3 * 1000);
                            }

                            for (int i = 0; i < 10; i ++) {
                                danmakuView.shotTextDanmakuAt("I am 5!", 5 * 1000);
                            }
                        }
                    }, 1000);
                } else {
                    danmakuView.stop();
                }
            }
        });

        Button closeSwitcher = (Button) findViewById(R.id.OpenOrClose);
        closeSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(danmakuView.isHided()) {
                    danmakuView.show();
                } else {
                    danmakuView.hide();
                }
            }
        });

        Button pauseSwitcher = (Button) findViewById(R.id.PauseOrResume);
        pauseSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(danmakuView.isPaused()) {
                    danmakuView.resume();
                } else {
                    danmakuView.pause();
                }
            }
        });

        Button shot = (Button) findViewById(R.id.Shot);
        shot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 10; i ++) {
                    danmakuView.shotTextDanmaku("hello world!");
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
//        danmakuView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        danmakuView.pause();
    }

}
