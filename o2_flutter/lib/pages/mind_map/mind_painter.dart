import 'package:flutter/material.dart';
import 'dart:ui' as ui;
import '../../common/models/mindmap/mind_node.dart';
import '../../common/utils/o2_api_manager.dart';
import 'theme/mind_map_theme.dart';

class SelectNode {
  final NodePaintElement node;
  final RRectPaintElement selectRect;

  SelectNode(this.node, this.selectRect);
}

///
/// 网络图片异步加载
///
typedef RefreshImage = Function(String, String);


class MindMapPainter extends CustomPainter {

  final LinePaintElement? linePaintElement;
  final NodePaintElement root ;
  final RRectPaintElement? selectRect;

  final Map<String, ui.Image> mindMapImages;
  final Map<int, ui.Image> priorityImages;
  final Map<int, ui.Image> progressImages;
  final ui.Image? linkIconImage;
  ui.Paint myPaint =  ui.Paint();

  MindMapPainter({
    required this.root,
    required this.linePaintElement,
    required this.selectRect,
    required this.priorityImages,
    required this.progressImages,
    required this.linkIconImage,
    required this.mindMapImages
  });

  @override
  void paint(Canvas canvas, Size size) {
    paintNodeInner(canvas, root);
    paintLines(canvas);
    if (selectRect != null) {
      myPaint.style = selectRect!.style.style;
      myPaint.color = selectRect!.style.color;
      myPaint.strokeWidth = selectRect!.style.strokeWidth;
      canvas.drawRRect(selectRect!.rrect, myPaint);
    }
    
  }

  ///
  /// 画节点内部元素
  ///
  void paintNodeInner(Canvas canvas, NodePaintElement node) {
    final elements = node.paintElements;
    // 背景
    if(elements.containsKey(NodeElement.background)) {
      RRectPaintElement? back = elements[NodeElement.background] as RRectPaintElement?;
      if (back != null) {
        myPaint.style = back.style.style;
        myPaint.color = back.style.color;
        canvas.drawRRect(back.rrect, myPaint);
      }
    }
    if(elements.containsKey(NodeElement.border)) {
      RRectPaintElement? border = elements[NodeElement.border] as RRectPaintElement?;
      if (border != null) {
        myPaint.style = border.style.style;
        myPaint.color = border.style.color;
        myPaint.strokeWidth = border.style.strokeWidth;
        canvas.drawRRect(border.rrect, myPaint);
      }
    }

    // 画图片
    if(elements.containsKey(NodeElement.image)) {
      ImagePaintElement? imagePaint = elements[NodeElement.image] as ImagePaintElement?;
      if (imagePaint != null) {
        var url = node.data.image;
        if (node.data.imageId != null && node.data.imageId != 'null'  && node.data.imageId?.isNotEmpty == true) {
          url = O2ApiManager.instance.getFileURL(node.data.imageId!);
        }
        if(url != null && url.isNotEmpty && mindMapImages.containsKey(url)) {
          final image = mindMapImages[url];
          myPaint.style = imagePaint .style.style;
          myPaint.color = imagePaint.style.color;
  //        print('paint image ,url:$url');
          canvas.drawImageRect(
              image!,
              Rect.fromLTWH(0.0, 0.0, image.width.toDouble(), image.height.toDouble()),
              imagePaint.rect,
              myPaint);
        }
      }
      
    }
    // 优先级
    if(elements.containsKey(NodeElement.priority)) {
      ImagePaintElement? image = elements[NodeElement.priority] as ImagePaintElement?;
      if (image != null) {
        myPaint.style = image.style.style;
        myPaint.color = image.style.color;
        var iconImage = priorityImages[node.data.priority];
        canvas.drawImageRect(
            iconImage!,
            Rect.fromLTWH(0.0, 0.0, iconImage.width.toDouble(), iconImage.height.toDouble()),
            image.rect,
            myPaint);
      }
      
    }
    // 进度
    if(elements.containsKey(NodeElement.progress)) {
      ImagePaintElement? image = elements[NodeElement.progress] as ImagePaintElement?;
      if (image != null) {
        myPaint.style = image.style.style;
        myPaint.color = image.style.color;
        var iconImage = progressImages[node.data.progress];
        canvas.drawImageRect(
            iconImage!,
            Rect.fromLTWH(0.0, 0.0, iconImage.width.toDouble(), iconImage.height.toDouble()),
            image.rect,
            myPaint);
      }
      
    }
    // 文字
    TextPaintElement? text = elements[NodeElement.text] as TextPaintElement?;
    if (text != null) {
      text.painter.paint(canvas, text.offset!);
    }
    

    // 超链接
    if(elements.containsKey(NodeElement.hyperlink)) {
      ImagePaintElement? image = elements[NodeElement.hyperlink] as ImagePaintElement?;
      if (image != null && linkIconImage != null) {
        myPaint.style = image.style.style;
        myPaint.color = image.style.color;
        canvas.drawImageRect(
            linkIconImage!,
            Rect.fromLTWH(0.0, 0.0, linkIconImage!.width.toDouble(), linkIconImage!.height.toDouble()),
            image.rect,
            myPaint);
      }
      
    }

    if(node.children!=null && node.children?.isNotEmpty == true) {
      for(var i=0;i<node.children!.length;i++) {
        paintNodeInner(canvas, node.children![i]);
      }
    }
  }


  ///
  /// 画连接线
  ///
  void paintLines(Canvas canvas) {
    final lines = linePaintElement?.lines;
    if(lines !=null && lines.isNotEmpty) {
      myPaint.style = linePaintElement!.style.style;
      myPaint.strokeWidth = linePaintElement!.style.strokeWidth;
      myPaint.color = linePaintElement!.style.color;
      for (var line in lines) {
//          final bezierControlX = line.start.dx < line.end.dx ? line.start.dx + (line.end.dx - line.start.dx) / 3: line.start.dx - (line.start.dx - line.end.dx) / 3;
//          final bezierControlY = line.end.dy;

        final bifurcationX = line.start.dx < line.end.dx ? (line.end.dx - line.start.dx) / 2 : (line.start.dx - line.end.dx) / 2;
        final bifurcationPoint = line.start.dx < line.end.dx ? Offset(line.start.dx + bifurcationX, line.start.dy) :  Offset(line.start.dx - bifurcationX, line.start.dy);
        final turnPoint = Offset(bifurcationPoint.dx, line.end.dy);
//          final rect = Rect.fromLTWH(turnPoint.dx, turnPoint.dy, 8, 8);
//          final startAngle = pi;
//          final sweepAngle = bifurcationPoint.dy > turnPoint.dy ? 0.5 * pi : -0.5 * pi;
//          final Path path = Path()
//            ..moveTo(line.start.dx, line.start.dy)
//            ..lineTo(bifurcationPoint.dx, bifurcationPoint.dy)
//            ..lineTo(turnPoint.dx, turnPoint.dy)
//            ..addArc(rect, startAngle, sweepAngle)
//              ..lineTo(line.end.dx, line.end.dy);
////            ..quadraticBezierTo(bezierControlX, bezierControlY, line.end.dx, line.end.dy);
//          canvas.drawPath(path, myPaint);


        canvas.drawLine(line.start, bifurcationPoint, myPaint);
        canvas.drawLine(bifurcationPoint, turnPoint, myPaint);
        canvas.drawLine(turnPoint, line.end, myPaint);
      }
    }

  }


  @override
  bool shouldRepaint(CustomPainter oldDelegate) {
//    if(oldDelegate is MindMapPainter) {
//      // todo 这里有问题 每次oldDelegate里面的数据都没有变化
//      final thisMap = json.encode(this.root.toJson());
//      final oldMap = json.encode(oldDelegate.root.toJson());
//      final check = checkIsImageCacheChanged(oldDelegate.mindMapImages);
//      print('check:$check , size:${this.mindMapImages.length} , old:${oldDelegate.mindMapImages.length}');
//      if(thisMap == oldMap
//          && this.selectRect == oldDelegate.selectRect
//          && !check
//          && !checkLinesChanged(oldDelegate.linePaintElement)) {
//        return false;
//      }
//    }
    return this != oldDelegate;
  }

  ///
  /// 图片资源是否有变化
  ///
  bool checkIsImageCacheChanged(Map<String, ui.Image> oldMap) {
    if (mindMapImages.length != oldMap.length) {
      return true;
    }
    for(var url in mindMapImages.keys) {
      bool isInOldMap = false;
      for(var oldUrl in oldMap.keys) {
        if (url == oldUrl) {
          isInOldMap = true;
        }
      }
      if (!isInOldMap) {
        return true;
      }
    }
    return false;
  }

  bool checkLinesChanged(LinePaintElement oldlinePaint) {
    if(linePaintElement?.lines.length != oldlinePaint.lines.length) {
      return true;
    }
    if(linePaintElement?.style.style != oldlinePaint.style.style
        || linePaintElement?.style.color != oldlinePaint.style.color
        || linePaintElement?.style.strokeWidth != oldlinePaint.style.strokeWidth) {
      return true;
    }
    var list = linePaintElement?.lines ?? [];
    var oldlist = oldlinePaint.lines;
    for(var line in list) {
      bool isInOld = false;
      for(var oldline in oldlist) {
        if(line.start == oldline.start && line.end == oldline.end) {
          isInOld = true;
        }
      }
      if(!isInOld) {
        return true;
      }
    }
    return false;
  }
}
