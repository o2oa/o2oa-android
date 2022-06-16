import 'dart:ui' as ui;
import 'package:flutter/painting.dart';
import 'package:o2_flutter/common/models/mindmap/mind_node.dart'; 

enum ElementType {
  text,
  image,
  rect,
  rrect,
  circle,
  line
}
///
/// 画笔样式
/// 每个对象的画笔样式 就是画笔的一些属性 比如color style strokeWidth等等
///
class PaintStyle {
  static const double defaultStrokeWidth = 2.0;

  final ui.Color color;
  final ui.PaintingStyle style;
  final double strokeWidth;
  PaintStyle({ required this.color, required this.style, this.strokeWidth = defaultStrokeWidth });
}

///
/// 需要画出来的元素
///
class PaintElement {
  final ElementType type;
  PaintElement(this.type);
}
///
/// 文字对象
///
class TextPaintElement extends PaintElement {
  TextPainter painter;
  Offset? offset;
  TextPaintElement(this.painter): super(ElementType.text);
}
///
/// 图片对象
///
class ImagePaintElement extends PaintElement {
  ui.Rect rect;
  PaintStyle style;
  ui.Image? image;
  ImagePaintElement(this.rect, this.style): super(ElementType.image);
}
///
/// 方块对象
///
class RectPaintElement extends PaintElement {
  ui.Rect rect;
  PaintStyle style;
  RectPaintElement(this.rect, this.style): super(ElementType.rect);
}
///
/// 圆角方块对象
///
class RRectPaintElement extends PaintElement {
  ui.RRect rrect;
  PaintStyle style;
  RRectPaintElement(this.rrect, this.style): super(ElementType.rrect);
}
///
/// 圆对象
///
class CirclePaintElement extends PaintElement {
  ui.Offset center;
  double radius;
  PaintStyle style;
  CirclePaintElement(this.center, this.radius, this.style): super(ElementType.circle);
}
///
/// 线条对象
///
class LinePaintElement extends PaintElement {
  List<NodeConnectLine> lines;
  PaintStyle style;
  LinePaintElement(this.lines, this.style): super(ElementType.line);

}


///
/// 节点绘画对象
///
class NodePaintElement {

  static const rootParent ='-1';
  static const rootId = 'root';
  static const rootLevel = 0;

  int level;
  NodeData data;
  Map<NodeElement, PaintElement> paintElements;
  Size nodeSize; // 节点本身大小
  Offset? offset;// 节点offset
  Size? childrenSize; // 所有子节点占的大小范围 这个需要在模版中计算
  List<NodePaintElement>? children;
  NodePaintElement({required this.level, required this.data, required this.paintElements, required this.nodeSize});

  Map<String, dynamic> toJson() {
    var map = Map<String, dynamic>();
    if(children!=null && children?.isNotEmpty == true) {
      var jsonChildren = <Map<String, dynamic>>[];
      for(var i=0;i<children!.length;i++) {
        jsonChildren.add(children![i].toJson());
      }
      map['children'] = jsonChildren;
    }
    map['data'] = data.toJson();
    return map;
  }
}

///
/// 节点连接线
///
class NodeConnectLine {
  final Offset start;
  final Offset end;
  NodeConnectLine(this.start, this.end);
}

abstract class BaseTheme {


  final String name;
  final double nodeVerticalSpace;
  final double nodeHorizontalSpace;
  double lineWidth = 2.0; //默认线条宽度
  double elementGap = 5.0;// 节点内部元素的间距
  double linkIconSize = 20.0; // 超链接图标
  double progressIconSize = 20.0; //进度图片大小
  double priorityIconSize = 20.0;// 优先级图片大小

  double rootRectPadding = 20.0;
  // 中心节点字体大小
  double rootFontSize = 22.0;
  // 二级节点
  double secondRectPadding = 10.0;
  // 二级节点文字大小
  double secondFontSize = 18.0;
  // 三级以及后面的节点
  double nodeRectPadding = 5.0;
  // 节点文字大小
  double nodeFontSize = 14.0;


  ///color
  // 画布背景色
  Color canvasBackgroundColor = const Color.fromARGB(255, 255, 255, 255); //默认白色背景

  // 线条颜色 二级节点边框线颜色
  Color lineColor = const Color.fromARGB(255, 115, 161, 191);


  BaseTheme(this.name, this.nodeVerticalSpace, this.nodeHorizontalSpace);

  NodePaintElement calElementSize(Node root);
  NodePaintElement sizeNode(NodeData data, int level);

}
