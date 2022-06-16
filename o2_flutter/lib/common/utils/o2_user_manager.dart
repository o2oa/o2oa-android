import 'package:o2_flutter/common/models/o2_user.dart';

import 'shared_preference_manager.dart';
import 'dart:convert' show json;


class O2UserManager {

  static final O2UserManager instance = O2UserManager._internal();

  factory O2UserManager() => instance;

  O2UserManager._internal();


  O2User? _o2user;

  O2User? get o2User => _o2user;
  set o2User(O2User? user) {
    _o2user = user;
  }

  //初始化当前登录用户信息
  Future<void> initUser(String userJson) async {
    print('initUser:$userJson');
    String? user = await SharedPreferenceManager.instance.putString(SharedPreferenceManager.FLUTTER_O2_USER_KEY, userJson);
    if(user != null) {
      _o2user = O2User.fromJson(json.decode(user));
    }
  }

}