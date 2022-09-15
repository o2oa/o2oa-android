import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show SystemNavigator;

AppBar systemPopAppBar(String title, {List<Widget> actions = const []}) {
  return AppBar(
    title: Container(
      margin: const EdgeInsets.symmetric(vertical: 1.0, horizontal: 1.0),
      child: Row(
        children: <Widget>[
          IconButton(
              icon: const Icon(Icons.close),
              onPressed: () {
                SystemNavigator.pop();
              }),
          Text(title),
        ],
      ),
    ),
    actions: actions,
  );
}