import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../o2.dart';


class SharedPreferenceManager {
  static final SharedPreferenceManager instance  = SharedPreferenceManager._internal();

  SharedPreferenceManager._internal();

  factory SharedPreferenceManager() => instance;

  static const String FLUTTER_O2_USER_KEY = 'FLUTTER_O2_USER_KEY';
  static const String FLUTTER_O2_UNIT_KEY = 'FLUTTER_O2_UNIT_KEY';
  static const String FLUTTER_O2_CENTER_SERVER_KEY = 'FLUTTER_O2_CENTER_SERVER_KEY';
  static const String FLUTTER_O2_THEME_KEY = 'FLUTTER_O2_THEME_KEY';

  String? _theme;
  get theme {
    return _theme;
  }

  //初始化主题色
  Future<void> initTheme(String? theme) async {
    debugPrintStack(label: 'initTheme:$theme');
    if(theme != null) {
      _theme = await putString(FLUTTER_O2_THEME_KEY, theme);
    }else {
      _theme = redThemeKey;
    }
  }


  // String
  Future<String?> putString(String key, String? stringValue) async {
    SharedPreferences preferences = await SharedPreferences.getInstance();
    String? oldValue = preferences.getString(key);
    if (stringValue != null && stringValue != oldValue) {
      preferences.setString(key, stringValue);
    } else {
      stringValue = oldValue;
    }
    return stringValue;
  }
}