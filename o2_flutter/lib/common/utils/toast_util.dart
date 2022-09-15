import 'package:toast/toast.dart';
import 'package:flutter/material.dart';

class ToastHelper {

  static void init(BuildContext context) {
    ToastContext().init(context);
  }
  ///
  /// error 消息 背景是红色的
  ///
  static void showError(BuildContext context, String message) {
    Toast.show(message, duration: Toast.lengthLong, gravity: Toast.bottom, backgroundColor: Colors.red);
  }

  ///
  /// 普通消息
  ///
  static void showInfo(BuildContext context, String message) {
    Toast.show(message, duration: Toast.lengthShort, gravity: Toast.bottom, backgroundColor: Colors.lightBlue);
  }


}