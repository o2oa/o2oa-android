
import 'package:fluro/fluro.dart';
import 'route_handler.dart';

class Routes {
  static String mindMap = '/mindMap';
  static String mindMapView = '/mindMap/:id';
  static String errorLoad = '/error';

  static void configureRoutes(FluroRouter router) {
    router.notFoundHandler = notFoundHandler;
    router.define(errorLoad, handler: errorLoadHandler);
    router.define(mindMap, handler: mindMapHandler);
  }
}