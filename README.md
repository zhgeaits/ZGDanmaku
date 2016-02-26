# ZGDanmaku

android danmaku/danmu/tanmu implemented by opengl es 2.0

现在android上的弹幕以后有很多开源的实现了，基本上能够满足大众的需求，但是由于我们业务需求，那些弹幕的性能达不到我们的要求了，于是我们想要改用新的渲染方式实现，那就是opengles。

可惜在github找不到这样的开源项目，于是自己着手学习一下opengles，并简单实现了一个弹幕。

有空再写一下实现的原理，这里代码里面的注释已经很全了。

## TODO

1.启动和停止的优化  

2.缓存策略的修改  

3.帧率计算  

4.根据帧率变化速度，处理丢帧情况

5.bug fix  

6.创建bitmap优化

......



### Dependencies

由于glsurfaceview不能介于view的中间层，要么底层，要么顶层，是做不到处于中间层，然后透明的，所以需要使用TextureView。



GLTextureView：https://github.com/eaglesakura/gltextureview.git