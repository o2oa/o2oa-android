import 'package:flutter/material.dart';

// Flow 的布局delegate 上下叠加
class CameraFlowDelegate extends FlowDelegate {
  @override
  void paintChildren(FlowPaintingContext context) {
    for (var i = 0; i < context.childCount; i++) {
      context.paintChild(i, transform: Matrix4.translationValues(0, 0, 0));
    }
  }

  @override
  bool shouldRepaint(FlowDelegate oldDelegate) {
    return oldDelegate != this;
  }
}