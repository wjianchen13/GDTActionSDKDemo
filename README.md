## andorid广点通SDK接入

这个是广点通官方demo，这里记录一下接入的流程，以备以后需要和排查问题时查阅  
参考链接：https://developers.e.qq.com/docs/user_actions/apply/convertion_app

# 广点通SDK接入步骤

## 下载SDK压缩包
下载Android SDK的压缩包，压缩包中的GDTActionSDK.min.x.x.x.jar文件为您的App需要嵌入的SDK  
链接https://developers.e.qq.com/docs/user_actions/apply/convertion_app 的点击下载SDK和说明文档  

## android studio 导入方式
找到您的App工程下的libs文件夹，将下载到的SDK的jar包GDTActionSDK.min.x.x.x.jar拷贝到该目录下。  
然后在Android Studio中找到您的App所在的module目录，修改它的build.gradle文件，在dependencies下面添加SDK的依赖，如下所示。  
```Groovy
...
dependencies {
    compile files('libs/GDTActionSDK.min.x.x.x.jar')
}
...
```

## 添加SDK需要的权限
找到您的App所在module的目录，修改AndroidManifest.xml文件，添加权限： 
```Groovy
<uses-permission android:name="android.permission.INTERNET" /> <!-- 允许联网 -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/> <!-- 检测联网方式，区分设备当前网络是2G、3G、4G还是WiFi -->
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" /> <!-- 获取MAC地址，和设备标识一起作为用户标识 -->  
<uses-permission android:name="android.permission.READ_PHONE_STATE"/> <!-- 获取设备标识，标识用户  -->
```
## SDK要求的系统版本
SDK要求最低系统版本为API 14（Android 4.0）。

## 混淆配置
如果您开启了混淆，请加入下面的配置：
```Groovy
-dontwarn com.qq.gdt.action.**
-keep class com.qq.gdt.action.** {*;}
```
## SDK初始化
SDK的初始化分两步，第一步是调用GDTAction.init方法来初始化SDK；第二步是调用GDTAction.logAction(ActionType.START_APP);来上报App启动行为。下面以GDTActionSDKDemo工程为例进行说明。  

## 初始化例子
找到您的App的Application类（这里以GDTActionApplication类为例），在onCreate()方法中调用下面这段代码来初始化SDK，初始化相关的信息在Logcat会输出，请开发者关注。  
```Java
package com.qq.gdt.action.demo;

import android.app.Application;
import com.qq.gdt.action.GDTAction;

public class GDTActionApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    // ...
    GDTAction.init(this, "yourUserActionSetID", "yourAppSecretKey"); // 第一个参数是Context上下文，第二个参数是您在DMP上获得的行为数据源ID，第三个参数是您在DMP上获得AppSecretKey
  }
}
```
注意问题
1.调用位置：GDTAction.init初始化方法，必须在Application类的onCreate方法中调用。否则SDK将无法成功初始化，也无法准确上报App的启动和激活行为。（从1.3.0版本开始，SDK的GDTAction.init接口不再需要获取动态权限，只需要确保调用位置正确。如果没有在指定位置调用init方法，SDK会输出错误日志。）  
2.调用顺序：GDTAction.init初始化方法，必须在GDTAction.logAction行为上报方法的调用之前成功调用，否则GDTAction.logAction不会上报行为。
5.2 上报App启动
找到Activity类（这里以GDTActionLauncherActivity类为例）的onResume()方法，调用下面的代码来上报App启动行为。  
```Java
GDTAction.logAction(ActionType.START_APP); // 传入的actionType参数必须是ActionType.START_APP
```
其他具体接入相关内容参考：广点通DMP行为数据上报AndroidSDK接入文档.html（下载到本地打开）























