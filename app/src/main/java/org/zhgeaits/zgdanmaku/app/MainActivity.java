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
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import org.zhgeaits.zgdanmaku.utils.TexturePool;
import org.zhgeaits.zgdanmaku.view.GLTextureView;
import org.zhgeaits.zgdanmaku.view.IZGDanmakuView;
import org.zhgeaits.zgdanmaku.view.ZGDanmakuTextureView;
import org.zhgeaits.zgdanmaku.view.ZGDanmakuView;

public class MainActivity extends Activity {

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

        setContentView(R.layout.activity_main);

        Button glsurfaceview = (Button) findViewById(R.id.GLSurfaceView);
        glsurfaceview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(gContext, GLSurfaceViewActivity.class);
                startActivity(intent);
            }
        });

        Button gltextureview = (Button) findViewById(R.id.GLTextureView);
        gltextureview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(gContext, GLTextureViewActivity.class);
                startActivity(intent);
            }
        });

        Button gltextureview2 = (Button) findViewById(R.id.GLTexture2View);
        gltextureview2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(gContext, GLTextureView2Activity.class);
                startActivity(intent);
            }
        });
    }

}
