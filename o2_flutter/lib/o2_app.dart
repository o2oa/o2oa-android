
import 'package:fluro/fluro.dart';
import 'package:flutter/material.dart';
import 'package:o2_flutter/common/routers/application.dart';
import 'package:o2_flutter/common/routers/routers.dart';
import 'package:o2_flutter/common/utils/o2_api_manager.dart';
import 'package:o2_flutter/common/utils/o2_user_manager.dart';
import 'package:o2_flutter/common/utils/shared_preference_manager.dart';
import 'package:o2_flutter/common/utils/toast_util.dart';
import 'package:o2_flutter/o2.dart';

///
/// 根据路由跳转到对应的页面
///
class O2App extends StatefulWidget {
  final String route;
  O2App(this.route, {Key? key}) : super(key: key) {
    // 初始化路由
    final router = FluroRouter();
    Routes.configureRoutes(router);
    AppRouterManager.instance.initRouter(router);
    // 创建一个通道 和 原生通信
    O2MethodChannelManager.instance.initMethodChannel();
  }

  @override
  State<StatefulWidget> createState()  => _O2AppState();
}


class _O2AppState extends State<O2App> {
 
  MaterialColor primarySwatch = o2RedSwatch;
  bool isInit = false;
  String toRoute = '';

  void setTheme() {
    //初始化主题
    String? theme = SharedPreferenceManager.instance.theme;
    if (theme != null && theme == blueThemeKey) {
      primarySwatch = o2BlueSwatch;
    }
  }
  /// 读取从 android端 同步过来的服务器信息
  void initO2Config() async {
    try {
      var map = await O2MethodChannelManager.instance.methodChannel.invokeMethod(method_name_o2_config);
      if (map != null) {
        debugPrintStack(label: '找到了Native的频道。。。。。。。。');
        if (map is Map) {
          if (map.containsKey(param_name_o2_theme)) {
            await SharedPreferenceManager.instance
                .initTheme(map[param_name_o2_theme]);
          }
          if (map.containsKey(param_name_o2_user)) {
            await O2UserManager.instance.initUser(map[param_name_o2_user]);
          }
          if (map.containsKey(param_name_o2_unit)) {
            await O2ApiManager.instance.initO2Unit(map[param_name_o2_unit]);
          }
          if (map.containsKey(param_name_o2_center_server)) {
            await O2ApiManager.instance
                .initO2CenterServer(map[param_name_o2_center_server]);
          }
        } else {
          debugPrintStack(label: '.....不知道是啥。。。');
        }
        setState(() {
          setTheme();
          isInit = true;
        });
         
      } else {
         setState(() {
          setTheme();
          isInit = true;
          // 错误页面
          toRoute = Routes.errorLoad;
        });
      }
    } catch (e) {
      print(e);
      setState(() {
          setTheme();
          isInit = true;
          // 错误页面
          toRoute = Routes.errorLoad;
        });
    }
  }
  
  @override
  void initState() {
    super.initState();
    toRoute = widget.route;
    initO2Config();
  }

  
  @override
  Widget build(BuildContext context) {
    if (isInit) {
      return MaterialApp(
        title: '',
        theme: ThemeData(
          primarySwatch: primarySwatch,
        ),
        home:  O2SplashPage(toRoute),
        onGenerateRoute: AppRouterManager.instance.router?.generator,
      );
    } else {
      return Container(
        color: Colors.white,
        child: const Center(child: CircularProgressIndicator()),
      );
    }
  }

}

class O2SplashPage extends StatefulWidget {
  final String route;

  const O2SplashPage(this.route, {Key? key}) : super(key: key);

  @override
  State<StatefulWidget> createState() {
    return _O2SplashPageState();
  }
}


class _O2SplashPageState extends State<O2SplashPage> {
  _O2SplashPageState();


  @override
  void initState() {
    super.initState();
    countDown();
  }

  @override
  Widget build(BuildContext context) {
    // 初始化
    ToastHelper.init(context);
    
    return Scaffold(
      body: Container(
        color: Colors.white,
        child: const Center(child: CircularProgressIndicator()),
      ),
    );
  }

  void countDown() {
    var duration = const Duration(milliseconds: 500);
    Future.delayed(duration, routeTo);
  }

  void routeTo() {
    debugPrintStack(label: 'route To ...route: $widget.route');
    AppRouterManager.instance.router?.navigateTo(context,  widget.route, clearStack: true);
  }
}
