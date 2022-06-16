

import 'package:fluro/fluro.dart';
import 'package:o2_flutter/pages/mind_map/mind_map_index.dart';
import 'package:o2_flutter/pages/page_404.dart';
import 'package:o2_flutter/pages/page_error.dart';


/// 404
var notFoundHandler = Handler(
    handlerFunc: (context, Map<String, List<String>> params) {
  return O2PageNotFound();
});
var errorLoadHandler = Handler(
    handlerFunc: (context, Map<String, List<String>> params) {
  return const O2PageError();
});

var mindMapHandler = Handler(
    handlerFunc: (context, Map<String, List<String>> params) {
  return const MindMapHomePage();
});