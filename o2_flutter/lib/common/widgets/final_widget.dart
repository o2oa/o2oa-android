import 'package:flutter/material.dart';

class O2UI {
  //color
  static const Color backgroundColor = Color(0xFFF5F5F5);
  static const Color splitLineColor = Color(0xFFE0E0E0);
  static const Color textHintColor = Color(0xFF999999);
  static const Color textPrimaryColor = Color(0xFF666666);
  static const Color textPrimaryDarkColor = Color(0xFF333333);
  static const Color iconColor = Color(0xFF4D4D4D);
  static const Color dividerColor = Colors.black26;

  static const Color taskGroupDarkBackgroundColor =
      Color.fromARGB(255, 45, 53, 55);
  static const Color noteDarkBackgroundColor = Color.fromARGB(255, 61, 69, 71);

  //style
  static const TextStyle hintTextStyle = TextStyle(color: textHintColor);
  static const TextStyle primaryTextStyle = TextStyle(color: textPrimaryColor);
  static const TextStyle whiteTextStyle = TextStyle(color: Colors.white);
  static const TextStyle calendarWeeklyStyle =
      TextStyle(color: textHintColor, fontSize: 14);
  static const TextStyle calendarNormalStyle =
      TextStyle(color: textPrimaryColor, fontSize: 12);
  static const TextStyle calendarNotThisMonthStyle =
      TextStyle(color: textHintColor, fontSize: 12);
  static const TextStyle calendarSelected =
      TextStyle(color: Colors.white, fontSize: 12);

  static const TextStyle fontTitleSize = TextStyle(fontSize: 18);

  //widget
  static final Widget separatorView = Container(
    height: 0.5,
    color: dividerColor,
  );
  static const Widget divider =  SizedBox(height: 32.0);

  static const Widget dividerLine = SizedBox(height: 1.0);

  static const Widget emptyDataView =
      Center(child: Text('空空如也！', style: O2UI.hintTextStyle));

}
