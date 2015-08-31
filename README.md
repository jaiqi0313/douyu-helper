
## 简介

项目地址： https://github.com/hengyunabc/douyu-helper

交流QQ群： 312383777

## 功能

录制斗鱼直播的视频，支持同时录制多个房间，支持自动重试。

## 环境要求
* chrome浏览器或者兼容chrome扩展的浏览器（实测360极速浏览器可以正常运行）
* java运行环境

## 使用方法

* 安装java环境，如果已经安装了的不用安装

https://www.java.com/zh_CN/download/help/index_installing.xml

* 下载程序包，解压

https://github.com/hengyunabc/douyu-helper/releases

如果下载有问题，可以加QQ群下载。

* 安装chrome扩展

扩展在chrome-extendsion目录下，在chrome地址栏上输入“chrome://extensions/”，然后开启开发者模式“Developer Mode”，然后加载解压的扩展“Load unpacked extendsion”，选择chrome-extendsion目录。

加载成功的话，会在浏览器右上角显示一个斗鱼的图标。

如果是其它的浏览器，比如360极速浏览器，则可以在地址栏打开“chrome://myextensions/extensions”来安装扩展。

* 启动程序，输入要录制的房间号

启动脚本在bin目录下，双击start.bat启动。录制的录像在bin/video目录下。

启动之后输入录制的房间号，比如67373，这时浏览器会自动在第一个标签打开67373的房间，不断的刷新，直到主播开播了，获取到了视频的下载地址，才会停止刷新。

chrome扩展会在窗口里新开一个tab，不断地去尝试打开要下载的房间号，这是获取直播视频下载地址的方法，不要觉得奇怪。

## 获取直播房间号的方法

如果不知道房间号，在直播间的标题右边，有一个”房间举报“，占击就可以在打开的网页里看到房间号。

## 工作原理
chrome扩展不断地刷新要录制的房间，当房间开播时，就会获取到视频的url，这时会停止刷新。把这个视频的url提交给程序，然后程序启动http请求去下载视频。

## 注意事项
* 只能启动一个录制程序

因为扩展要和程序通迅，程序要侦听7373端口，所以只能启动一个程序。不过可以同时录制多个房间。

* 清晰度

浏览器里的清晰度是什么，获取的下载的url的清晰度也是一样的，所以想要录像“超清”的视频，要把浏览器里的斗鱼视频设置为”超清“。

* 标签停留在”about:blank“的问题

当开始下载时，没有房间需要刷新了，这时就会标签就会停留在”about:blank“。这时可以检查下程序是不是开始下载了。

