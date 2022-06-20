
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

///
/// 主题色
///
const String blueThemeKey = 'blue';
const String redThemeKey = 'red';
const Color o2Red = Color.fromARGB(255, 251, 71, 71);
const Color o2Blue = Color.fromARGB(255, 0, 139, 230);
const Color o2Dark = Color.fromARGB(255, 58, 60, 65);
final MaterialColor o2RedSwatch =  MaterialColor(o2Red.value,
  const <int, Color>{
    50:  o2Red,
    100: o2Red,
    200: o2Red,
    300: o2Red,
    400: o2Red,
    500: o2Red,
    600: o2Red,
    700: o2Red,
    800: o2Red,
    900: o2Red,
  },
);
final MaterialColor o2BlueSwatch =  MaterialColor(o2Blue.value,
  const <int, Color>{
    50:  o2Blue,
    100: o2Blue,
    200: o2Blue,
    300: o2Blue,
    400: o2Blue,
    500: o2Blue,
    600: o2Blue,
    700: o2Blue,
    800: o2Blue,
    900: o2Blue,
  },
);
///
/// web服务器上下文
///
const String o2_desktop_context = 'x_desktop';

///
/// o2服务器返回成功标记
///
const String o2_http_success = 'success';

///
/// 每页数量
///
const int default_page_size = 30;
///
/// 分页查询第一页 标记
///
const String firstPageId = '(0)';

///
/// native通道
///
const String native_channel_name = 'net.o2oa.flutter/native_get';
///
/// native传输的方法名称
///
// 获取配置信息 包括user、unit、webServer、assembleServer
const String method_name_o2_config = 'o2Config';

/// 
/// native传输的方法名称
/// 这个方法调用原生代码 打开图片选择器
const String method_name_o2_pick_image = 'o2PickImage';

///
/// native传输的字段key
///
// 主题色 blue red
const String param_name_o2_theme = 'o2Theme';
// user json字段
const String param_name_o2_user = 'o2UserInfo';
// unit json字段
const String param_name_o2_unit = 'o2UnitInfo';
// web server json字段
const String param_name_o2_center_server = 'o2CenterServerInfo';

const String param_name_o2_picker_image_file = 'file';



class O2MethodChannelManager {
  static final O2MethodChannelManager _instance = O2MethodChannelManager._internal();
  // 私有化
  O2MethodChannelManager._internal();

  static O2MethodChannelManager get instance  => _instance;
  
  late MethodChannel methodChannel;

  void initMethodChannel() {
     //创建一个通道，通道的name字符串要和Native端的一样
    methodChannel = const MethodChannel(native_channel_name);

  }
}