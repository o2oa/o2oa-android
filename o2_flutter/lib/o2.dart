
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
