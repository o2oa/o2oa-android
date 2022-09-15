
import 'package:fluro/fluro.dart';

class AppRouterManager {

  static final AppRouterManager instance = AppRouterManager._internal();

  factory AppRouterManager() => instance;

  AppRouterManager._internal();
  FluroRouter? _router;

  void initRouter(FluroRouter router) {
    _router = router;
  }

  FluroRouter? get router => _router;

}