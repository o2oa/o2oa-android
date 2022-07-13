import 'package:flutter/material.dart';


class O2SnackBars {

  static void showSnackBar(GlobalKey<ScaffoldState> _scaffoldKey, String message) {
    _scaffoldKey.currentState
        ?.showSnackBar(SnackBar(content: Text(message), duration: const Duration(milliseconds: 1500)));
  }

  static void showSnackBarWithContext(BuildContext context, String message) {
    Scaffold.of(context).showSnackBar(SnackBar(content: Text(message), duration: const Duration(milliseconds: 1500)));
  }
}