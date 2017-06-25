# Biscuit
[![](https://jitpack.io/v/pruas/Biscuit.svg)](https://jitpack.io/#pruas/Biscuit)

`Biscuit`是一个便捷的`android` 压缩图片库。由于微信是行业标杆，所以在写本库的时候，特意研究了下微信的压缩效果，以在小米`NOTE LTE`上为例，经过观察微信压缩效果，逆向推算出微信可能的压缩方式,发现微信很大概率上采用缩放压缩方式。于是本库采用两种压缩方式（采样率、缩放）供使用者选择使用，默认是采用和微信一样的缩放压缩方式并且效果非常接近！详见下面比较！

# 功能
* 可以传入一张或一个列表进行压缩
* 可以自定义保存路径
* 可以自定义压缩后是否使用原图名字命名
* 可以自定义压缩质量范围
* 可以选择缩放压缩或者采样率压缩
* 可以自定义执行器
* 可以自定义是否忽略透明度（忽略则质量差些，大小也将减小一半）
* 压缩前检查是否会引发OOM风险，避免程序Crash
* 可以清除缓存
* 压缩后拓展名不变。
* 可以控制log输出

# 压缩效果对比

先一睹为快！下图是微信的压缩效果：

![](https://github.com/pruas/Biscuit/blob/master/wechat_compressed.png)

然后这是Biscuit的压缩效果：

![](https://github.com/pruas/Biscuit/blob/master/biscuit_compressed.png)

上图中八张图片压缩数据对比：

原图 | `Biscuit` | `Wechat`
---- | ------ | ------
3120*4160/2.96MB|960*1280/61.58KB|960*1280/61.49KB
1080*9594/6.12MB|1019*9054/880.59KB|1019*9048/801.13KB
1080*5712/3.12MB|1080*5712/622.3KB|1080*5712/621.7KB
1080*2904/311KB|1080*2904/202.8KB|1080*2904/213.24KB
1080*1920,805KB|720*1280/122.2KB|720*1280/118.7KB
3120*4160/3.3MB|960*1280/100.56KB|960*1280/99.18KB
3120*4160/3.39MB|960*1280/93.5KB|960*1280/93.87KB
4160*3120/3.28MB|1280*960/72.57KB|1280*960/71.08KB

可以看到压缩后的图片宽高和微信非常一致！！！图片大小也几乎相等！！！

# Usage
Step 1. Add it in your root build.gradle at the end of repositories:
```gradle
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
Step 2. Add the dependency
```gradle
	dependencies {
	        compile 'com.github.pruas:Biscuit:v1.0.1'
	}
```
Step 3. Use it wherever you need
```java
                  Biscuit.with(this)
                        .path(photos)
                        .listener(mCompressListener)//压缩监听
                        .build();
```
Or you can customize like this
```java
                Biscuit.with(this)
                        .path(photos) //可以传入一张图片路径，也可以传入一个图片路径列表
                        .loggingEnabled(true)//是否输出log 默认输出
//                        .quality(50)//质量压缩值（0...100）默认已经非常接近微信，所以没特殊需求可以不用自定义
                        .originalName(true) //使用原图名字来命名压缩后的图片，默认不使用原图名字,随机图片名字
                        .listener(mCompressListener)//压缩监听
                        .targetDir(FileUtils.getImageDir())//自定义压缩保存路径
//                        .executor(executor) //自定义实现执行，注意：必须在子线程中执行 默认使用AsyncTask线程池执行
//                        .ignoreAlpha(true)//忽略alpha通道，对图片没有透明度要求可以这么做，默认不忽略。
//                        .compressType(Biscuit.SAMPLE)//采用采样率压缩方式，默认是使用缩放压缩方式，也就是和微信的一样。
                        .build();
```
Clear cache:
```java
Biscuit.clearCache(this);// default
```
or
```java
Biscuit.clearCache(FileUtils.getImageDir());//when you have set custom dir
```
# 说明
本库是在单一手机上测试，小米`Note 1080*1920`，所以如果你在本库过程中遇到什么问题，欢迎给我提`Issues` 。
