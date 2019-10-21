package com.qq.gdt.action.demo;

import android.app.Application;

import com.qq.gdt.action.GDTAction;

public class GDTActionApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    // 调用SDK初始化接口：
    // 1. 调用位置：必须在Application的onCreate方法中调用。
    // 2. 必须在其他的数据上报接口调用之前调用，否则其他接口都将无法使用。
    // 注意：从1.3.0版开始，SDK权限检查动静分离。init接口不要求申请动态权限，只要求调用位置正确，且只需要调用一次即可，第一次调用之后的任何调用都将无效。
    GDTAction.init(this, Constants.USER_ACTION_SET_ID, Constants.APP_SECRET_KEY);
  }

}
