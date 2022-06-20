import 'package:flutter/material.dart';
import 'final_widget.dart';

enum O2DialogAction { positive, cancel }

class O2Dialogs {

  static Future<O2DialogAction?> showConfirmDialog({
    required BuildContext context,
    required String message,
    String positiveBtnText = '确定',
    String cancelBtnText = '取消',
  }) {
    return showDialog<O2DialogAction>(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: const Text('提示'),
            content: Text(message),
            actions: <Widget>[
              TextButton(
                style: TextButton.styleFrom(
                  primary: O2UI.textHintColor,
                ),
                child: Text(cancelBtnText),
                onPressed: () {
                  Navigator.pop(context, O2DialogAction.cancel);
                },
              ),
              TextButton(
                child: Text(positiveBtnText),
                onPressed: () {
                  Navigator.pop(context, O2DialogAction.positive);
                },
              ),
              // FlatButton(
              //   textColor: O2UI.textHintColor,
              //   child: Text(cancelBtnText),
              //   onPressed: () {
              //     Navigator.pop(context, O2DialogAction.cancel);
              //   },
              // ),
              // FlatButton(
              //   child: Text(positiveBtnText),
              //   onPressed: () {
              //     Navigator.pop(context, O2DialogAction.positive);
              //   },
              // ),
            ],
          );
        });
  }

  static Future<O2DialogAction?> showCustomDialog({
    required BuildContext context,
    required String title,
    required Widget content,
    String positiveBtnText = '确定',
    String cancelBtnText = '取消',
  }) {
    return showDialog<O2DialogAction>(
        context: context,
        builder: (context) {
          return AlertDialog(
            title: Text(title),
            content: content,
            actions: <Widget>[
               TextButton(
                style: TextButton.styleFrom(
                  primary: O2UI.textHintColor,
                ),
                child: Text(cancelBtnText),
                onPressed: () {
                  Navigator.pop(context, O2DialogAction.cancel);
                },
              ),
              TextButton(
                child: Text(positiveBtnText),
                onPressed: () {
                  Navigator.pop(context, O2DialogAction.positive);
                },
              ),
              // FlatButton(
              //   textColor: O2UI.textHintColor,
              //   child: Text(cancelBtnText),
              //   onPressed: () {
              //     Navigator.pop(context, O2DialogAction.cancel);
              //   },
              // ),
              // FlatButton(
              //   child: Text(positiveBtnText),
              //   onPressed: () {
              //     Navigator.pop(context, O2DialogAction.positive);
              //   },
              // ),
            ],
          );
        });
  }
}
