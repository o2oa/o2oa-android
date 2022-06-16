

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:o2_flutter/o2.dart';

///
/// 根据路由跳转到对应的页面
///
class O2App extends StatefulWidget {
  final String route;
  O2App(this.route);

  @override
  State<StatefulWidget> createState() {
    return _O2AppState(this.route);
  }
}


class _O2AppState extends State<O2App> {
  //创建一个通道，通道的name字符串要和Native端的一样
  static const methodChannel = MethodChannel(native_channel_name);

  final String route;

  _O2AppState(this.route);


  void initO2Config() async {
    try {
      var map = await methodChannel.invokeMethod(method_name_o2_config);
      if (map != null) {
        print('找到了Native的频道。。。。。。。。');
        if (map is Map) {
          if (map.containsKey(param_name_o2_theme)) {
            var theme = map[param_name_o2_theme];
            print(theme);
          }
          if (map.containsKey(param_name_o2_user)) {
            var user = map[param_name_o2_user];
            print(user);
          }
          if (map.containsKey(param_name_o2_unit)) {
            var unit = map[param_name_o2_unit];
            print(unit);
          }
          if (map.containsKey(param_name_o2_center_server)) {
            var server = map[param_name_o2_center_server];
            print(server);
          }
        } else {
          print('.....不知道是啥。。。');
        }
      } else {
        print('没有找到Native的频道。。。。。。。。');
      }
    } catch (e) {
      print(e);
    }
  }

  
  @override
  void initState() {
    super.initState();
    print("进来的路由： $route");
    initO2Config();
  }

  
  @override
  Widget build(BuildContext context) {
    return Container(
        color: Colors.white,
        child: const Center(child: CircularProgressIndicator()),
      );
  }

}