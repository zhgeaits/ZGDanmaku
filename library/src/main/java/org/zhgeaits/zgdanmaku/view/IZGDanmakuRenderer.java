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
package org.zhgeaits.zgdanmaku.view;


import org.zhgeaits.zgdanmaku.model.ZGDanmaku;

import java.util.List;

/**
 * Created by zhgeaits on 16/2/26.
 * 弹幕渲染器接口
 */
public interface IZGDanmakuRenderer {

    List<ZGDanmaku> getRendererDanmakuList();

    void setRendererDanmakuList(List<ZGDanmaku> rendererDanmakuList);

    boolean isOKToRenderer();

    int getViewWidth();

    int getViewHeight();

    void setListener(IZGRenderListener listener);

    void setSpeed(float speed);

    void setHide(boolean hide);

    boolean isHide();

    void resume();

    void setPaused(boolean paused);
}
