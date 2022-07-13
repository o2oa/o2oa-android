import 'dart:ui';

import 'package:o2_flutter/common/models/mindmap/mind_node.dart';
import 'package:o2_flutter/pages/mind_map/theme/mind_map_theme.dart';


///
/// 脑图结构 模版
///
abstract class BaseTemplate {
  final String name;
  BaseTheme theme;

  BaseTemplate(this.name, this.theme);

  ///
  /// 计算画板的大小 根据内部节点元素大小来计算
  /// 每个模版结构不一样 计算方式不一样
  /// @return 画布大小
  ///
  Size canvasSize(NodePaintElement root);

  ///
  /// 计算每个需要画出来的元素在画布中的位置
  /// 每个模版结构不一样 位置也不一样
  /// @return 返回各个节点之间的连接线
  ///
  LinePaintElement? paintElementPosition(NodePaintElement root, Size canvasSize);
}

///
/// 默认模版
/// 默认模版分 左右两边，第三级开始没有节点的背景只有一条底边
///
class DefaultTemplate extends BaseTemplate {

  late Size rightChildrenSize;
  late Size leftChildrenSize;

  DefaultTemplate(theme) : super('default', theme);

  @override
  Size canvasSize(NodePaintElement root) {
    var width = root.nodeSize.width;
    var height = root.nodeSize.height;
    final len = root.children?.length;
    if (len == null || len == 0) {
      return Size(width*1.5, height*1.5);
    }
    var rightLen = 0;
    if (len > 2) {
      if (len % 2 == 0) {
        rightLen = len ~/ 2;
      } else {
        rightLen = len ~/ 2 + 1;
      }
    }else {
      rightLen = 1;
    }
    var rightList = root.children?.sublist(0, rightLen) ?? [];
    var leftList = len-rightLen>0 ? root.children?.sublist(rightLen, len) ?? [] : <NodePaintElement>[];
    // 右边子节点
    var rightWidth = 0.0;
    var rightHeight = 0.0;
    if(rightList.isNotEmpty) {
      for (var right in rightList) {
        var childSize = childNodeSize(
            right, theme.nodeVerticalSpace, theme.nodeHorizontalSpace);
        rightWidth =
        rightWidth > childSize.width ? rightWidth : childSize.width;
        rightHeight += childSize.height + theme.nodeHorizontalSpace;
      }
      rightHeight -= theme.nodeHorizontalSpace;
    }
    rightChildrenSize = Size(rightWidth, rightHeight);
    // 左边子节点
    var leftWidth = 0.0;
    var leftHeight = 0.0;
    if(leftList.isNotEmpty) {
      for (var left in leftList) {
        var childSize = childNodeSize(
            left, theme.nodeVerticalSpace, theme.nodeHorizontalSpace);
        leftWidth = leftWidth > childSize.width ? leftWidth : childSize.width;
        leftHeight += childSize.height + theme.nodeHorizontalSpace;
      }
      leftHeight -= theme.nodeHorizontalSpace;
    }
    leftChildrenSize = Size(leftWidth, leftHeight);

    ///
    /// root节点本身的宽+2个间隔+left和right最大的宽度*2
    /// root节点本身的高 和 left、right最大的高度*2
    ///
    width += (rightWidth > leftWidth ? rightWidth : leftWidth) * 2 + theme.nodeHorizontalSpace * 2;
    height += (rightHeight > leftHeight ? rightHeight : leftHeight) * 2;

    return Size(width*1.5, height*1.5);
  }

  ///
  /// 递归计算子节点的大小
  ///
  Size childNodeSize(NodePaintElement node, double verticalSpace, double horizontalSpace) {
    var width = node.nodeSize.width;
    var height = node.nodeSize.height;
    // 模版中第三级开始 有一条底部边线
    if (node.level>1) {
      height += theme.lineWidth;
    }
    var childrenWidth = 0.0;
    var childrenHeight = 0.0;
    if(node.children != null && node.children?.isNotEmpty == true) {
      for(var child in node.children!) {
        var childSize = childNodeSize(child, verticalSpace, horizontalSpace);
        childrenWidth = childrenWidth > childSize.width ? childrenWidth:childSize.width;
        childrenHeight += childSize.height + horizontalSpace;
      }
      childrenHeight -= horizontalSpace;
    }
    final childSize = Size(childrenWidth, childrenHeight);
    node.childrenSize = childSize;
    width += childrenWidth + verticalSpace;
    height = height>childrenHeight ? height : childrenHeight;

    return Size(width, height);

  }

  @override
  LinePaintElement? paintElementPosition(NodePaintElement root, Size canvasSize) {
    final centerPoint = Offset(canvasSize.width/2, canvasSize.height/2);
    //确定root的offset
    final rootTop = centerPoint.dy-root.nodeSize.height/2;
    final rootLeft = centerPoint.dx-root.nodeSize.width/2;
    root.offset = Offset(rootLeft, rootTop);
    nodeElementsPosition(rootTop, rootLeft, root);

    final len = root.children?.length;
    if (len == null || len == 0) {
      return null;
    }
    List<NodeConnectLine> lines = [];
    var rightLen = 0;
    if (len > 2) {
      if (len % 2 == 0) {
        rightLen = len ~/ 2;
      } else {
        rightLen = len ~/ 2 + 1;
      }
    }else {
      rightLen = 1;
    }
    final rightList = root.children?.sublist(0, rightLen) ?? [];
    final leftList = len-rightLen>0 ? root.children?.sublist(rightLen, len) ?? [] : <NodePaintElement>[];

    if(rightList.isNotEmpty) {
      final rightCenterPoint = Offset(centerPoint.dx + root.nodeSize.width/2, centerPoint.dy);
      rightNodePosition(rightList, rightChildrenSize, lines, rightCenterPoint, rightCenterPoint);
    }
    if(leftList.isNotEmpty) {
      final leftCenterPoint = Offset(centerPoint.dx - root.nodeSize.width/2, centerPoint.dy);
      leftNodePosition(leftList, leftChildrenSize, lines, leftCenterPoint, leftCenterPoint);
    }

    return LinePaintElement(lines, PaintStyle(color: theme.lineColor, strokeWidth: theme.lineWidth, style: PaintingStyle.stroke));
  }

  ///
  /// rootRightStart root节点的右边中心点 也是连接右边子节点的线条起点
  ///
  void rightNodePosition(List<NodePaintElement> rightList, Size allListSize, List<NodeConnectLine> lines, Offset parentRightCenter, Offset parentLineStart) {
    final firstNode = rightList[0];
    final firstNodeAndChildrenRegionHeight = firstNode.nodeSize.height > (firstNode.childrenSize?.height ?? 0) ?
        firstNode.nodeSize.height : (firstNode.childrenSize?.height ?? 0);
    final lastNode = rightList[rightList.length - 1];
    final lastNodeAndChildrenRegionHeight = lastNode.nodeSize.height > (lastNode.childrenSize?.height ?? 0) ?
        lastNode.nodeSize.height :  (lastNode.childrenSize?.height?? 0);
    // 第一个子节点的中心点Y轴
    final centerGap = (allListSize.height - (firstNodeAndChildrenRegionHeight/2 + lastNodeAndChildrenRegionHeight/2))/2;
    final firstNodeCenterY = parentRightCenter.dy - (centerGap>0 ? centerGap : 0);
    final nodeLeft = parentRightCenter.dx + theme.nodeVerticalSpace;
    var nodeTop = 0.0; // 每个节点的top
    var y = 0.0; // node中心y
    for (var i=0;i<rightList.length;i++) {
      final rightNode = rightList[i];
      if (i == 0) {
        y = firstNodeCenterY;
      }else {
        final preNodeHeight = (rightList[i-1].childrenSize?.height ?? 0) > rightList[i-1].nodeSize.height ? (rightList[i-1].childrenSize?.height ?? 0) : rightList[i-1].nodeSize.height;
        final nodeHeight = (rightNode.childrenSize?.height ?? 0) > rightNode.nodeSize.height ? (rightNode.childrenSize?.height ?? 0) : rightNode.nodeSize.height;
        y = y + preNodeHeight /2 + nodeHeight/2 + theme.nodeHorizontalSpace;
      }
      nodeTop = y - rightNode.nodeSize.height/2;
      rightNode.offset = Offset(nodeLeft, nodeTop);
      nodeElementsPosition(nodeTop, nodeLeft, rightNode);
      final lineEndTop = rightNode.level>1 ? y + rightNode.nodeSize.height/2 + theme.lineWidth : y;
      // 第三级开始有底边
      if (rightNode.level>1) {
        lines.add(NodeConnectLine( Offset(nodeLeft, lineEndTop), Offset(nodeLeft + rightNode.nodeSize.width, lineEndTop)
        ));
      }
      lines.add(NodeConnectLine( parentLineStart,  Offset(nodeLeft, lineEndTop)));

      if (rightNode.children!=null && rightNode.children?.isNotEmpty == true) {
        rightNodePosition(
            rightNode.children!,
            rightNode.childrenSize ?? const Size(0, 0),
            lines,
            Offset(nodeLeft+rightNode.nodeSize.width, y),
            Offset(nodeLeft+rightNode.nodeSize.width, lineEndTop));
      }
    }

  }

  void leftNodePosition(List<NodePaintElement> leftList, Size allListSize, List<NodeConnectLine> lines, Offset parentLeftCenter, Offset parentLineStart) {
    final firstNode = leftList[0];
    final firstNodeAndChildrenRegionHeight = firstNode.nodeSize.height > (firstNode.childrenSize?.height ?? 0) ?
    firstNode.nodeSize.height : (firstNode.childrenSize?.height ?? 0) ;
    final lastNode = leftList[leftList.length - 1];
    final lastNodeAndChildrenRegionHeight = lastNode.nodeSize.height > (lastNode.childrenSize?.height ?? 0) ?
    lastNode.nodeSize.height : (lastNode.childrenSize?.height ?? 0);
    // 第一个子节点的中心点Y轴
    final centerGap = (allListSize.height - (firstNodeAndChildrenRegionHeight/2 + lastNodeAndChildrenRegionHeight/2))/2;
    final firstNodeCenterY = parentLeftCenter.dy -  (centerGap>0 ? centerGap : 0);

    final nodeRight = parentLeftCenter.dx - theme.nodeVerticalSpace;

    var nodeTop = 0.0; // 每个节点的top
    var nodeLeft = 0.0;
    var y = 0.0; // node 中心y
    for (var i=0;i<leftList.length;i++) {
      final leftNode = leftList[i];
      if (i == 0) {
        y = firstNodeCenterY;
      }else {
        final preNodeHeight = (leftList[i-1].childrenSize?.height ?? 0) > leftList[i-1].nodeSize.height ? (leftList[i-1].childrenSize?.height ?? 0) : leftList[i-1].nodeSize.height;
        final nodeHeight = (leftNode.childrenSize?.height ?? 0) > leftNode.nodeSize.height ? (leftNode.childrenSize?.height ?? 0) : leftNode.nodeSize.height;
        y = y + preNodeHeight /2 + nodeHeight/2 + theme.nodeHorizontalSpace;
      }
      nodeTop = y - leftNode.nodeSize.height/2;
      nodeLeft = nodeRight - leftNode.nodeSize.width;
      leftNode.offset = Offset(nodeLeft, nodeTop);
      nodeElementsPosition(nodeTop, nodeLeft, leftNode);
      final lineEndTop = leftNode.level>1 ? y + leftNode.nodeSize.height/2 + theme.lineWidth : y;
      // 第三级开始有底边
      if (leftNode.level>1) {
        lines.add(NodeConnectLine(Offset(nodeLeft, lineEndTop), Offset(nodeRight,
                lineEndTop)
        ));
      }
      lines.add(NodeConnectLine(parentLineStart,
         Offset(nodeRight, lineEndTop)
      ));

      if (leftNode.children!=null && leftNode.children?.isNotEmpty == true) {
        leftNodePosition(
            leftNode.children!,
            leftNode.childrenSize ?? const Size(0, 0),
            lines,
            Offset(nodeLeft, y),
            Offset(nodeLeft, lineEndTop));
      }
    }
  }

  ///
  /// 计算节点内部的元素位置
  ///
  void nodeElementsPosition(double top, double left, NodePaintElement node) {
    Map<NodeElement, PaintElement> elements = node.paintElements;
    if(elements.isNotEmpty) {
      // 外边框
      if(elements.containsKey(NodeElement.border)) {
        var border = elements[NodeElement.border];
        if (border != null) {
           if(border is RRectPaintElement) {
            border.rrect = RRect.fromLTRBR(left, top, left + border.rrect.width, top + border.rrect.height, border.rrect.blRadius);
          }
          elements[NodeElement.border] = border;
        }
      }
      // 背景
      if(elements.containsKey(NodeElement.background)) {
        var back = elements[NodeElement.background];
        if (back != null) {
          if(back is RRectPaintElement) {
            var backLeft = left;
            var backTop = top;
            if(elements.containsKey(NodeElement.border)) {
              backLeft += theme.lineWidth;
              backTop  += theme.lineWidth;
            }
            back.rrect = RRect.fromLTRBR(backLeft, backTop, backLeft+back.rrect.width, backTop+back.rrect.height, back.rrect.blRadius);
          }
          elements[NodeElement.background] = back;
        }
        
      }
      // 文字
      final text = elements[NodeElement.text] as TextPaintElement;
      final textWidth = text.painter.width;
      final textHeight = text.painter.height;
      var bottomWidth = textWidth;
      var bottomHeight = textHeight;
      if(elements.containsKey(NodeElement.priority)) {
        bottomWidth += theme.priorityIconSize + theme.elementGap;
        bottomHeight = bottomHeight > theme.priorityIconSize ? bottomHeight : theme.priorityIconSize;
      }
      if(elements.containsKey(NodeElement.progress)) {
        bottomWidth += theme.progressIconSize + theme.elementGap;
        bottomHeight = bottomHeight > theme.progressIconSize ? bottomHeight : theme.progressIconSize;
      }
      if(elements.containsKey(NodeElement.hyperlink)) {
        bottomWidth += theme.linkIconSize + theme.elementGap;
        bottomHeight = bottomHeight > theme.linkIconSize ? bottomHeight : theme.linkIconSize;
      }
      double leftPadding = (node.nodeSize.width - bottomWidth) / 2;
      double textLeft = left + leftPadding;
      double rectPadding = theme.nodeRectPadding;
      if(node.level == 0) {
        rectPadding = theme.rootRectPadding;
      }else if(node.level == 1) {
        rectPadding = theme.secondRectPadding;
      }
      // 优先级
      if(elements.containsKey(NodeElement.priority)) {
        var priority = elements[NodeElement.priority] as ImagePaintElement;
        var center = bottomHeight > theme.priorityIconSize ? (bottomHeight - theme.priorityIconSize)/2 : (theme.priorityIconSize-bottomHeight)/2;
        var priorityTop = top + node.nodeSize.height - rectPadding - bottomHeight + center;
        priority.rect = Rect.fromLTWH(left+leftPadding, priorityTop, theme.priorityIconSize, theme.priorityIconSize);
        elements[NodeElement.priority] = priority;
        textLeft += theme.priorityIconSize + theme.elementGap;
      }
      // 文字
      var textTopGap = bottomHeight > textHeight ? (bottomHeight - textHeight)/2 : (textHeight-bottomHeight)/2;
      var textTop = top + node.nodeSize.height - rectPadding - bottomHeight + textTopGap;
      text.offset = Offset(textLeft, textTop);
      elements[NodeElement.text] = text;
      // 进度
      if(elements.containsKey(NodeElement.progress)) {
        var progress = elements[NodeElement.progress] as ImagePaintElement;
        var center = bottomHeight > theme.progressIconSize ? (bottomHeight - theme.progressIconSize)/2 : (theme.progressIconSize-bottomHeight)/2;
        var progressTop = top + node.nodeSize.height - rectPadding - bottomHeight + center;
        progress.rect = Rect.fromLTWH(
            textLeft + textWidth + theme.elementGap,
            progressTop,
            theme.progressIconSize,
            theme.progressIconSize);
        elements[NodeElement.progress] = progress;
      }
      // 超链接
      if(elements.containsKey(NodeElement.hyperlink)) {
        var hyperlink = elements[NodeElement.hyperlink] as ImagePaintElement;
        var center = bottomHeight > theme.linkIconSize ? (bottomHeight - theme.linkIconSize)/2 : (theme.linkIconSize-bottomHeight)/2;
        var hyperlinkTop = top + node.nodeSize.height - rectPadding - bottomHeight + center;
        var hyperlinkLeft = elements.containsKey(NodeElement.progress) ? textLeft + textWidth + theme.progressIconSize + theme.elementGap*2 : textLeft + textWidth + theme.elementGap;
        hyperlink.rect = Rect.fromLTWH(
            hyperlinkLeft,
            hyperlinkTop,
            theme.linkIconSize,
            theme.linkIconSize);
        elements[NodeElement.hyperlink] = hyperlink;
      }
      // 图片
      if(elements.containsKey(NodeElement.image)) {
        var image = elements[NodeElement.image] as ImagePaintElement;
        image.rect = Rect.fromLTWH(left+(node.nodeSize.width - image.rect.width) / 2, top+rectPadding, image.rect.width, image.rect.height);
        elements[NodeElement.image] = image;
      }
      node.paintElements = elements;
    }
  }




}