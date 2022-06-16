

import 'package:fluro/fluro.dart';
import 'package:o2_flutter/pages/page_404.dart';


/// 404
var notFoundHandler = Handler(
    handlerFunc: (context, Map<String, List<String>> params) {
  return O2PageNotFound();
});