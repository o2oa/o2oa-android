
import 'package:fluro/fluro.dart';
import 'application.dart';
import 'route_handler.dart';

class Routes {
  static String mindMap = '/app/mindmap';
  static String mindMapView = '/app/mindmap/:id';
  static String login = '/login';
  static String calendar = '/calendar/index';
  static String teamWork = '/teamwork/index';
  static String teamWorkHD = '/teamworkhd/index';
  static String teamWorkHDProjectTask = '/teamworkhd/project/task';
  static String teamWorkProject = '/teamwork/project/';
  static String teamWorkProjectAdd = '/teamwork/project/:id';
  static String teamWorkProjectTask = '/teamwork/projecttask';
  static String teamWorkTask = '/teamwork/task';
  static String identitySelector = '/org/identity/selector';

  static void configureRoutes(FluroRouter router) {
    router.notFoundHandler = notFoundHandler;
  }
}