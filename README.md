# ZGDanmaku

This is an android library which use opengles 2.0 to renderer danmaku effect.

## 初衷/Introduction

在Github上能找到很多开源的弹幕库，并且有很多是优秀的实现，基本上能够满足大众的需求，但是我却找不到使用opengles实现渲染的。于是我就自己着手学习一下opengles，并简单实现了一个弹幕。

## 效果/Demo



Powering in YY:


## 实现原理/Understanding Android OpenGLES

- [Android OpenGLES学习之入门理解](http://zhgeaits.me/android/2014/10/16/android-opengles-study-notes.html)

- [Android OpenGLES学习之实现弹幕渲染](http://zhgeaits.me/android/2016/02/27/android-opengles-danmaku-study-notes.html)

## TODO

1.修改弹幕逻辑

2.修改为时间同步策略

### Dependencies

1.由于glsurfaceview不能介于view的中间层，要么底层，要么顶层，是做不到处于中间层，然后透明的，所以需要使用TextureView。

GLTextureView：  

- [eaglesakura's GLTextureView](https://github.com/eaglesakura/gltextureview.git)
- [Google's GLTextureView](https://github.com/romannurik/muzei/blob/master/main/src/main/java/com/google/android/apps/muzei/render/GLTextureView.java)

2.使用了[DanmakuFlameMaster的创建bitmap方法](https://github.com/Bilibili/DanmakuFlameMaster)  

### License

Copyright (C) 2016 Zhang Ge <zhgeaits@gmail.com>
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
