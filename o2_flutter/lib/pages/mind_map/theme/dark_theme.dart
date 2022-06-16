import 'package:flutter/painting.dart';
import 'package:o2_flutter/common/models/mindmap/mind_node.dart';
import 'package:o2_flutter/o2.dart';
import 'mind_map_theme.dart';

///
/// 黑暗主题
///
class DarkTheme extends BaseTheme {

  DarkTheme(): super('dark-theme', 66.0, 44.0) {
    //暗黑背景色
    canvasBackgroundColor = o2Dark;
    // 线条颜色
    lineColor = const Color.fromARGB(255, 217, 217, 217);

    //文字样式
    rootTextStyle = TextStyle(
      color: rootTextColor,
      fontSize: rootFontSize,
      fontWeight: FontWeight.bold,
    );
    secondTextStyle = TextStyle(
        color: secondTextColor,
        fontSize: secondFontSize
    );
    nodeTextStyle = TextStyle(
        color: nodeTextColor,
        fontSize: nodeFontSize
    );
  }

  final scale = 1.0;

  // 中心节点
  final _rectCircularBase = 5.0;//圆角
  double get rectCircular => _rectCircularBase * scale;//圆角



  // 中心节点填充颜色
  Color rootRectColor = const Color.fromARGB(255, 89, 149, 247);
  // 中心节点文字颜色
  Color rootTextColor = const Color(0xFFFFFFFF);
  // 中心节点文字样式
  late TextStyle rootTextStyle;

  // 二级节点填充颜色
  Color secondRectColor = const Color.fromARGB(255, 255, 255, 255);
  // 二级节点文字颜色
  Color secondTextColor = const Color.fromARGB(255, 68, 68, 68);
  // 节点文字样式
  late TextStyle secondTextStyle;

  // 节点文字颜色
  Color nodeTextColor = const Color.fromARGB(255, 251, 251, 251);
  // 节点文字样式
  late TextStyle nodeTextStyle;




  @override
  NodePaintElement calElementSize(Node root) {
      var data = root.data;
      NodePaintElement rootPaint = sizeNode(data, NodePaintElement.rootLevel);
      var children = root.children;
      var list = <NodePaintElement>[];
      for(var node in children) {
        var child = recursiveNode(node, NodePaintElement.rootLevel + 1);
        list.add(child);
      }
      rootPaint.children = list;
      return rootPaint;
  }

  ///
  /// 递归Node
  ///
  NodePaintElement recursiveNode(Node node, int level) {
      var data = node.data;
      NodePaintElement nodePaint = sizeNode(data, level);
      var children = node.children;
      if(children.isNotEmpty) {
        var list = <NodePaintElement>[];
        for(var child in children) {
          var ret = (recursiveNode(child, level + 1)); // children
          list.add(ret);
        }
        nodePaint.children = list;
      }
      return nodePaint;
    
  }


  ///
  /// Node内部元素 计算
  ///
  @override
  NodePaintElement sizeNode(NodeData data, int level) {
    var elements = Map<NodeElement, PaintElement>();
    var nodeWidth = 0.0;
    var nodeHeight = 0.0;
    ///文字
    ///
    if (level == 0) { //root
      var textPainter = TextPainter(
          text: TextSpan(style: rootTextStyle, text: data.text),
          textDirection: TextDirection.ltr,
          textAlign: TextAlign.center)
        ..layout();
      elements[NodeElement.text] =  TextPaintElement(textPainter);
      nodeHeight += textPainter.height;
      nodeWidth += textPainter.width;
    }else if (level == 1) { //
      var textPainter = TextPainter(
          text: TextSpan(style: secondTextStyle, text: data.text),
          textDirection: TextDirection.ltr,
          textAlign: TextAlign.center)
        ..layout();
      elements[NodeElement.text] =  TextPaintElement(textPainter);
      nodeHeight += textPainter.height;
      nodeWidth += textPainter.width;
    }else {
      var textPainter = TextPainter(
          text: TextSpan(style: nodeTextStyle, text: data.text),
          textDirection: TextDirection.ltr,
          textAlign: TextAlign.center)
        ..layout();
      elements[NodeElement.text] =  TextPaintElement(textPainter);
      nodeHeight += textPainter.height;
      nodeWidth += textPainter.width;
    }

    ///
    /// 进度
    ///
    if(data.progress != null && data.progress! > 0) {
      Rect rect = Rect.fromLTWH(0.0, 0.0, progressIconSize, progressIconSize);
      PaintStyle style = PaintStyle(color: rootTextColor, style: PaintingStyle.fill);
      elements[NodeElement.progress] = ImagePaintElement(rect, style);
      nodeHeight = nodeHeight > progressIconSize ? nodeHeight : progressIconSize;
      nodeWidth += progressIconSize + elementGap;
    }
    ///
    /// 优先级
    ///
    if(data.priority != null && data.priority! > 0) {
      Rect rect = Rect.fromLTWH(0.0, 0.0, priorityIconSize, priorityIconSize);
      PaintStyle style = PaintStyle(color: rootTextColor, style: PaintingStyle.fill);
      elements[NodeElement.priority] = ImagePaintElement(rect, style);
      nodeHeight = nodeHeight > priorityIconSize ? nodeHeight : priorityIconSize;
      nodeWidth += priorityIconSize + elementGap;
    }
    ///
    /// 超链接
    ///
    if(data.hyperlink != null ) {
      Rect rect = Rect.fromLTWH(0.0, 0.0, linkIconSize, linkIconSize);
      PaintStyle style = PaintStyle(color: rootTextColor, style: PaintingStyle.fill);
      elements[NodeElement.hyperlink] = ImagePaintElement(rect, style);
      nodeHeight = nodeHeight > linkIconSize ? nodeHeight : linkIconSize;
      nodeWidth += linkIconSize + elementGap;
    }

    ///
    /// 图片
    ///
    if( (data.image != null && data.image!.isNotEmpty) || (data.imageId != null && data.imageId!.isNotEmpty)) {
      var size = data.imageSize ?? ImageSize(width: 40, height: 40);
      Rect rect = Rect.fromLTWH(0.0, 0.0, size.width.toDouble(), size.height.toDouble());
      PaintStyle style = PaintStyle(color: rootTextColor, style: PaintingStyle.fill);
      elements[NodeElement.image] = ImagePaintElement(rect, style);
      nodeHeight += size.height.toDouble() + elementGap;
      nodeWidth = size.width > nodeWidth ? size.width.toDouble() : nodeWidth;
    }

    ///
    /// 节点外框
    ///
    if (level == 0) { // root没有设置id
      data.id = NodePaintElement.rootId;
      nodeWidth += rootRectPadding*2;
      nodeHeight += rootRectPadding*2;
      Rect rect = Rect.fromLTWH(0.0, 0.0, nodeWidth, nodeHeight);
      PaintStyle style = PaintStyle(color: rootRectColor, style: PaintingStyle.fill);
      elements[NodeElement.background] = RRectPaintElement(RRect.fromRectAndRadius(rect, Radius.circular(rectCircular)), style);
    }else if(level == 1) { // 二级
      nodeWidth += secondRectPadding*2;
      nodeHeight += secondRectPadding*2;
      Rect rect = Rect.fromLTWH(0.0, 0.0, nodeWidth, nodeHeight);
      PaintStyle style = PaintStyle(color: secondRectColor, style: PaintingStyle.fill);
      elements[NodeElement.background] = RRectPaintElement(RRect.fromRectAndRadius(rect, Radius.circular(rectCircular)), style);

    }else {
      nodeWidth += nodeRectPadding*2;
      nodeHeight += nodeRectPadding*2;
    }

    return NodePaintElement(
        level: level,
        data:data,
        paintElements:elements,
        nodeSize:Size(nodeWidth, nodeHeight)
    );
  }


}