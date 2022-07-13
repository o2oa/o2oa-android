

import 'package:fluro/fluro.dart';
import 'package:flutter/material.dart';
import 'package:o2_flutter/pages/mind_map/mind_map_index.dart';
import 'package:o2_flutter/pages/page_404.dart';
import 'package:o2_flutter/pages/page_error.dart';

import '../../pages/mind_map/mind_map_view.dart';


/// 404
var notFoundHandler = Handler(
    handlerFunc: (context, Map<String, List<String>> params) {
  return const O2PageNotFound();
});
var errorLoadHandler = Handler(
    handlerFunc: (context, Map<String, List<String>> params) {
  return const O2PageError();
});

var mindMapHandler = Handler(
    handlerFunc: (context, Map<String, List<String>> params) {
  return const MindMapHomePage();
});
var mindMapViewHandler = Handler(
    handlerFunc: (context, Map<String, List<String>> params) {
  var id = params['id']?.first;
  debugPrint('mindMapViewHandler id: $id');
  if (id == null) {
    return const O2PageError();
  }
  return MindMapView(id);
});