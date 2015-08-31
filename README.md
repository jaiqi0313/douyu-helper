
## 简介

项目地址： https://github.com/hengyunabc/douyu-helper

## 使用方法

* 下载程序包，解压
* 安装chrome扩展

扩展在chrome-extendsion目录下，在chrome地址栏上输入“chrome://extensions/”，然后开启开发者模式“Developer Mode”，然后加载解压的扩展“Load unpacked extendsion”，选择chrome-extendsion目录。

加载成功的话，会在浏览器右上角显示一个斗鱼的图标。

* 启动程序，输入要录制的房间号

启动脚本在bin目录下，双击start.bat启动。

启动之后输入录制的房间号，比如67373，这时浏览器会自动在第一个标签打开67373的房间，不断的刷新，直到主播开播了，获取到了视频的下载地址，才会停止刷新。

chrome扩展会在窗口里新开一个tab，不断地去尝试打开要下载的房间号，这是获取直播视频下载地址的方法，不要觉得奇怪。