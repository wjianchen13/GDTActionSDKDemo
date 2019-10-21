package com.qq.gdt.action.demo;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.qq.gdt.action.ActionType;
import com.qq.gdt.action.GDTAction;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 如果是Android6.0以上的系统，调用GDTAction.logAction时，需要处理Android6.0动态权限的兼容问题。
 *
 * 这里是一个简单的示例，开发者可以根据自己的情况去适配动态权限。
 *
 * 在本示例中：
 * 我们需要在Activity的onResume方法中上报App启动行为。
 * - 如果是Android6.0以下的手机，直接调用logAction上报App启动行为；
 * - 如果是Android6.0以上（含6.0）的手机，需要检查App是否已经获取到动态权限。
 *   - 如果没有权限，那么需要申请`READ_PHONE_STATE`权限之后再上报App启动行为。
 *   - 如果已经获得权限，那么直接上报App启动行为。
 */
public class GDTActionLauncherActivity extends Activity
    implements View.OnClickListener, AdapterView.OnItemSelectedListener {

  private static final String TAG = GDTActionLauncherActivity.class.getSimpleName();
  Button btnSendStandardAction, btnSendCustomAction;
  Spinner spinnerStandardAction;
  ArrayAdapter<String> adapter;
  EditText editCustomAction;
  int spinnerPosition;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initViews();
    GDTAction.init(this, Constants.USER_ACTION_SET_ID, Constants.APP_SECRET_KEY);
  }

  @Override
  protected void onResume() {
    super.onResume();
    reportAppStart();
  }

  /**
   * 在onResume方法中，我们要上报App启动行为。
   * SDK内部会计算App退到后台的时间间隔，30秒内调用多次也只会上报一次启动事件。
   * 在Activity的onResume方法中上报App启动行为，是因为当用户按下home键使应用退到后台，再恢复到前台时，可能不会执行onCreate方法。
   *
   * 因为logAction接口需要检查动态权限，所以上报时分两种情况：
   * - 1.在Android6.0以上，需要检查动态权限，如果已经获取到了权限那么直接上报，如果没有获取到，那么等申请到动态权限再上报App启动。
   * - 2.在Android6.0以下，因为不需要申请动态权限，直接上报App启动行为即可。
   */
  private void reportAppStart() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      checkAndRequestPermission();
    } else {
      GDTAction.logAction(ActionType.START_APP);
    }
  }

  /**
   * ActionSDK必须要获得Manifest.permission.READ_PHONE_STATE权限，否则将无法获得手机的IMEI号来作为用户标识。
   *
   * 注意：如果没有这项权限ActionSDK不会工作。
   */
  @TargetApi(Build.VERSION_CODES.M)
  private void checkAndRequestPermission() {
    List<String> lackedPermission = new ArrayList<String>();
    if (!(checkSelfPermission(
        Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
      lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
    }

    // 如果已经有了权限，那么可以直接上报行为
    if (lackedPermission.size() == 0) {
      GDTAction.logAction(ActionType.START_APP);
    } else {
      // 请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限，如果获得权限就可以调用logAction，否则申请到权限之后再调用。
      String[] requestPermissions = new String[lackedPermission.size()];
      lackedPermission.toArray(requestPermissions);
      requestPermissions(requestPermissions, 1024);
    }
  }

  private boolean hasAllPermissionsGranted(int[] grantResults) {
    for (int grantResult : grantResults) {
      if (grantResult == PackageManager.PERMISSION_DENIED) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
      @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 1024 && hasAllPermissionsGranted(grantResults)) {
      // 获得用户授权，此时可以上报行为
      GDTAction.logAction(ActionType.START_APP);
    } else {
      // 如果用户没有授权，那么应该说明意图，引导用户去设置里面授权。
      Toast.makeText(this, "应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。", Toast.LENGTH_LONG).show();
      Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
      intent.setData(Uri.parse("package:" + getPackageName()));
      startActivity(intent);
      finish();
    }
  }

  private void initViews() {
    btnSendStandardAction = (Button) findViewById(R.id.send_standard_action);
    btnSendCustomAction = (Button) findViewById(R.id.send_custom_action);
    editCustomAction = (EditText) findViewById(R.id.edit_custom_action);
    btnSendCustomAction.setOnClickListener(this);
    btnSendStandardAction.setOnClickListener(this);
    //
    spinnerStandardAction = (Spinner) findViewById(R.id.spinner_standard_action);
    spinnerStandardAction.setOnItemSelectedListener(this);
    List<String> standardActionList = new ArrayList<String>();
    standardActionList.add(ActionType.PAGE_VIEW);
    standardActionList.add(ActionType.REGISTER);
    standardActionList.add(ActionType.VIEW_CONTENT);
    standardActionList.add(ActionType.CONSULT);
    standardActionList.add(ActionType.ADD_TO_CART);
    standardActionList.add(ActionType.PURCHASE);
    standardActionList.add(ActionType.SEARCH);
    standardActionList.add(ActionType.ADD_TO_WISHLIST);
    standardActionList.add(ActionType.INITIATE_CHECKOUT);
    standardActionList.add(ActionType.COMPLETE_ORDER);
    standardActionList.add(ActionType.DOWNLOAD_APP);
    standardActionList.add(ActionType.RATE);
    standardActionList.add(ActionType.RESERVATION);
    standardActionList.add(ActionType.SHARE);
    standardActionList.add(ActionType.APPLY);
    adapter =
        new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, standardActionList);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinnerStandardAction.setAdapter(adapter);
  }

  @Override
  public void onClick(View v) {
    String actionType;
    switch (v.getId()) {
      case R.id.send_standard_action:
        actionType = adapter.getItem(spinnerPosition);
        GDTAction.logAction(actionType);
        // 如果您需要在上报转化行为的同时，上报行为参数，可以传入一个JSONObject对象
        // 例如，用户发生购物行为时，可以用GDTAction.logAction上报用户的这次行为，并将价格等行为参数一起带上
//        try {
//          JSONObject actionParam = new JSONObject();
//          actionParam.put("value", 6800);
//          actionParam.put("name", "Pixel 2 XL");
//          GDTAction.logAction(actionType, actionParam);
//        } catch (JSONException e) {
//          e.printStackTrace();
//        }
        break;
      case R.id.send_custom_action:
        actionType = editCustomAction.getText().toString();
        if (!TextUtils.isEmpty(actionType)) {
          GDTAction.logAction(actionType);
        }
        break;
      default:
        break;
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    spinnerPosition = position;
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {

  }

}
