import 'package:flutter/material.dart';
import 'package:flutter/services.dart' show rootBundle;
import 'dart:ui' as ui;
import 'dart:io';
import 'package:image/image.dart' as imageTool;
import 'package:path_provider/path_provider.dart';
import 'dart:convert';
import '../../common/models/image_picker_type.dart';
import '../../common/models/mindmap/mind_map.dart';
import '../../common/models/mindmap/mind_node.dart';
import '../../common/utils/o2_api_manager.dart';
import '../../common/utils/x_file_assemble_control.dart';
import '../../common/utils/x_mind_assemble_control.dart';
import '../../common/widgets/dialogs.dart';
import '../../common/widgets/final_widget.dart';
import '../../common/widgets/flow_delegate.dart';
import '../../common/widgets/loading.dart';
import '../../common/widgets/mind_map_hyperlink.dart';
import '../../common/widgets/snack_bars.dart';
import '../../common/widgets/transparent_app_bar_widget.dart';
import '../../o2.dart';
import 'mind_painter.dart';
import 'mind_map_input.dart';
import 'mind_map_data.dart';
import 'theme/mind_map_theme.dart';

class MindMapView extends StatefulWidget {
  final String mapId;
  const MindMapView(this.mapId, {Key? key}) : super(key: key);
  @override
  MindMapViewState createState() {
    return MindMapViewState();
  }
}

class MindMapViewState extends State<MindMapView>
    with SingleTickerProviderStateMixin {
  final GlobalKey globalKey = GlobalKey();
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();
  AnimationController? movePositionController;
  Animation<Offset>? tween;

  Color actionBarItemColor = Colors.black87; // 默认顶部操作栏上的按钮和标题颜色
  MindMap? _mindMap;
  MindMapData? map;
  Size? canvasSize;
  Size? mediaSize;
  LinePaintElement? linePaintElement;
  NodePaintElement? node;
  SelectNode? selectNode;
  bool inputViewIsHidden = true;
  bool autoFocus = false;
  double inputLeft = 0.0;
  double inputTop = 0.0;
  double inputWidth = 0.0;
  double inputHeight = 0.0;
  Color inputFillColor = Colors.white;
  TextSpan? textSpan;

  ui.Image? linkIconImage;
  var priorityImages = Map<int, ui.Image>();
  var progressImages = Map<int, ui.Image>();
  Map<String, ui.Image> mindMapImages = Map<String, ui.Image>();
  int numPointers = 0;

  var scale = 1.0; // 放大缩小 最小0.2 最大2
  var lastScale = 1.0; // 多次放大缩小的时候保存上一次的结果。
  var scaling = false;

  //移动位置
  Offset? canvasPosition;
  Offset? lastPosition;
  Offset? startMovePosition;
  double maxNodeX = 0.0; // 所有节点中x位置最大的 这个需要加上节点的宽度 不然右边会看不到
  double minNodeX = 0.0; //所有节点中x位置最小的
  double maxNodeY = 0.0; //所有节点中y位置最大的 这个需要加上节点的高度 不然下面会看不到
  double minNodeY = 0.0; //所有节点中y位置最小的

  PersistentBottomSheetController? bottomSheetController;

  // 加载数据
  Future loadData() async {
    // 先加载资源图片
    await _loadAssetsImage();
    if (widget.mapId != null) {
      // 获取解析json数据
      _mindMap = await MindMapService().mindMap(widget.mapId);
      if (_mindMap != null) {
        map = MindMapData.fromJson(json.decode(_mindMap!.content!));
        //todo 根据主题背景色来判断处理actionBarItemColor的颜色
        actionBarItemColor = Colors.white;
        _reloadDataFromJson();
        setState(() {});
      } else {
        _showErrorMessage('获取脑图数据异常！');
      }
    } else {
      _showErrorMessage('id不存在无法加载数据！');
    }
  }

  void _saveMindMapThumbnail() async {
    if (_mindMap == null) {
      _showErrorMessage("数据为空 无法保存！");
      return;
    }
    Loading.start(context);
    //画板保存成图片
    var image = await _rendered();
    var pngBytes = await image.toByteData(format: ui.ImageByteFormat.png);
    if (pngBytes == null) {
      _showErrorMessage("数据为空 无法保存！");
      return;
    }
    //裁剪边上的空白
//    var newSize = canvasSize / 1.5;
//    var oldImage = imageTool.Image.fromBytes(canvasSize.width.toInt(), canvasSize.height.toInt(), pngBytes.buffer.asUint8List());
//    var x = (canvasSize.width - newSize.width) / 2;
//    var y = (canvasSize.height - newSize.height) / 2;
//    debugPrint('x:$x ,y:$y');
//    var newImage = imageTool.copyCrop(oldImage, x.toInt(), y.toInt(), newSize.width.toInt(), newSize.height.toInt());
    //图片临时存储
    var tempDirPath = await getTemporaryDirectory();
    File file = File('${tempDirPath.path}/${_mindMap!.id ?? 'tempId'}.png')
      ..writeAsBytesSync(pngBytes.buffer.asInt8List());

    FileAssembleService().uploadImageForMindMap(_mindMap!.id!, file).then((id) {
      if (id != null && id.isNotEmpty) {
        _mindMap!.icon = id;
      } else {
        print('保存缩略图失败。。。。。没有返回id');
      }
      _saveMindMap();
    }).catchError((error) {
      print('保存缩略图失败,$error');
      Loading.complete(context);
      _showErrorMessage('保存失败！！');
    });
  }

  void _saveMindMap() {
    MindMapService().saveMindMap(_mindMap, map).then((id) {
      Loading.complete(context);
      _showErrorMessage('保存成功！');
    }).catchError((error) {
      print('保存失败,$error');
      Loading.complete(context);
      _showErrorMessage('保存失败！');
    });
  }

  @override
  void initState() {
    print('initState................');
    super.initState();
    movePositionController =
        AnimationController(duration: const Duration(milliseconds: 200), vsync: this);
    movePositionController!.addListener(() {
      setState(() {
        canvasPosition = tween?.value;
      });
    });
    loadData();
  }

  @override
  void dispose() {
    movePositionController?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    var horizontalInitOffset = 0.0;
    var verticalInitOffset = 0.0;
    if (map != null && canvasSize != null) {
      mediaSize = MediaQuery.of(context).size;
      horizontalInitOffset = canvasSize!.width - mediaSize!.width > 0
          ? (canvasSize!.width - mediaSize!.width) / 2
          : 0.0;
      verticalInitOffset = canvasSize!.height - mediaSize!.height > 0
          ? (canvasSize!.height - mediaSize!.height) / 2
          : 0.0;
      canvasPosition ??= Offset(-horizontalInitOffset, -verticalInitOffset);
    }
    var title = '脑图';
    if (_mindMap != null) {
      title = _mindMap?.name ?? '脑图';
    }
    return Scaffold(
      key: _scaffoldKey,
      body: TransparentAppBarWidget(
          title: Text(
            title,
            style: TextStyle(color: actionBarItemColor, fontSize: 18),
          ),
          actions: _topBar(),
          backButtonColor: actionBarItemColor,
          body: map == null
              ? const Center(child: CircularProgressIndicator())
              : Stack(children: _contentViews())),
    );
  }

  ///
  /// 主屏内容
  ///
  List<Widget> _contentViews() {
    List<Widget> l = [];
    l.add(_canvasView());
    l.add(_bottomOperationBar());
    return l;
  }

  ///
  /// 脑图内容 包括一些事件
  ///
  Widget _canvasView() {
    return Listener(
      onPointerDown: (_) => numPointers++,
      onPointerUp: (_) => numPointers--,
      child: GestureDetector(
        child: Stack(
          // overflow: Overflow.clip,
          children: <Widget>[
            Container(
              color: map?.mapTheme.canvasBackgroundColor,
            ),
            Positioned(
              left: canvasPosition?.dx,
              top: canvasPosition?.dy,
              width: canvasSize?.width,
              height: canvasSize?.height,
              child: Transform.scale(
                scale: scale,
                child: Flow(
                  delegate: CameraFlowDelegate(),
                  children: <Widget>[
                    CustomPaint(
                      key: globalKey,
                      size: canvasSize!,
                      painter: MindMapPainter(
                        root: node!,
                        linePaintElement: linePaintElement,
                        selectRect: selectNode?.selectRect,
                        priorityImages: priorityImages,
                        progressImages: progressImages,
                        linkIconImage: linkIconImage,
                        mindMapImages: mindMapImages,
                      ),
                    ),
                    Offstage(
                      child: Stack(
                        children: <Widget>[
                          MindMapTextEdit(
                            inputLeft: inputLeft,
                            inputTop: inputTop,
                            inputWidth: inputWidth,
                            inputHeight: inputHeight,
                            autoFocus: autoFocus,
                            inputFillColor: inputFillColor,
                            textSpan: textSpan,
                            textEditDone: (text) {
                              var data = NodeData();
                              data.text = text;
                              _updateSelectData(data);
                            },
                          )
                        ],
                      ),
                      offstage: inputViewIsHidden,
                    )
                  ],
                ),
              ),
            )
          ],
        ),
        onScaleStart: _scaleStart,
        onScaleEnd: _scaleEnd,
        onScaleUpdate: _scaleUpdate,
        onTapUp: (detail) => _tapUp(context, detail),
      ),
    );
  }

  ///
  /// 顶部操作栏
  ///
  List<Widget> _topBar() {
    if (map == null) {
      return <Widget>[];
    } else {
      return <Widget>[
        IconButton(
            icon: Icon(
              Icons.save,
              color: actionBarItemColor,
            ),
            onPressed: () {
              _saveMindMapThumbnail();
            })
      ];
    }
  }

  ///
  /// 底部节点操作栏
  ///
  Widget _bottomOperationBar() {
    return Offstage(
        offstage: !inputViewIsHidden,
        child: Align(
          alignment: Alignment.bottomCenter,
          child: Offstage(
            offstage: selectNode == null,
            child: Container(
              color: const Color.fromARGB(255, 191, 194, 199),
              height: kToolbarHeight,
              constraints: const BoxConstraints.expand(height: kToolbarHeight),
              child: SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: Row(
                  children: <Widget>[
                    _bottomOperationButton(addChildNode, Icons.add, '下级'),
                    Offstage(
                        offstage: !(selectNode != null &&
                            selectNode!.node.data.id != 'root'),
                        child: _bottomOperationButton(
                            addBrotherNode, Icons.library_add, '同级')),
                    _bottomOperationButton(_editNodeText, Icons.edit, '编辑'),
                    Offstage(
                      offstage: !(selectNode != null &&
                          selectNode!.node.data.id != 'root'),
                      child: _bottomOperationButton(
                          deleteNode, Icons.delete_forever, '删除'),
                    ),
                    _bottomOperationButton(
                        _showIconTools, Icons.tag_faces, '图标'),
                    _bottomOperationButton(
                        _showImagePickMenu, Icons.image, '图片'),
                    _bottomOperationButton(_showLinkDialog, Icons.link, '超链接')
                  ],
                ),
              ),
            ),
          ),
        ));
  }

  ///
  /// 底部操作栏上的按钮
  ///
  Widget _bottomOperationButton(
      VoidCallback onPressed, IconData icon, String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(5, 3, 5, 3),
      child: RawMaterialButton(
          onPressed: onPressed,
          child: Flex(
              mainAxisAlignment: MainAxisAlignment.center,
              direction: Axis.horizontal,
              children: <Widget>[
                Icon(
                  icon,
                  color: O2UI.iconColor,
                ),
                Text(
                  title,
                  style: TextStyle(color: O2UI.iconColor),
                )
              ]),
          fillColor: Colors.white,
          shape: RoundedRectangleBorder(
              side: BorderSide.none,
              borderRadius: BorderRadius.all(Radius.circular(5))),
          materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
          padding: const EdgeInsets.all(5.0),
          constraints: const BoxConstraints(minWidth: 0.0, minHeight: 0.0)),
    );
  }

  Future _loadAssetsImage() async {
    priorityImages.clear();
    progressImages.clear();
    for (var i = 0; i < 10; i++) {
      var path = i == 0 ? 'images/priorityx.png' : 'images/priority$i.png';
      var progressPath =
          i == 0 ? 'images/progressx.png' : 'images/progress$i.png';
      var bytes = await rootBundle.load(path);
      var list = bytes.buffer.asUint8List();
      var image = await decodeImageFromList(list);
      priorityImages[i] = image;

      var progressBytes = await rootBundle.load(progressPath);
      var progressList = progressBytes.buffer.asUint8List();
      var progressImage = await decodeImageFromList(progressList);
      progressImages[i] = (progressImage);
    }
    var linkBytes = await rootBundle.load('images/link.png');
    linkIconImage = await decodeImageFromList(linkBytes.buffer.asUint8List());
  }

  ////////////////////////////event///////////////////////////////////
  //添加下级节点
  void addChildNode() {
    void _searchForAddChildNode(Node node) {
      if (node.data.id == selectNode?.node.data.id) {
        _addChild(node);
      } else {
        if (node.children.isNotEmpty) {
          for (var child in node.children) {
            _searchForAddChildNode(child);
          }
        }
      }
    }

    if (selectNode != null) {
      if (selectNode?.node.data.id == 'root' && map?.root != null) {
        _addChild(map!.root);
      } else {
        if (map?.root != null && map?.root.children != null && map?.root.children.isNotEmpty == true) {
          for (var child in map!.root.children) {
            _searchForAddChildNode(child);
          }
        }
      }
    } else {
      debugPrint('请选中节点。。。。');
    }
  }

  //添加同级节点
  void addBrotherNode() {
    bool _searchForAddBrotherNode(Node node) {
      if (node.children.isNotEmpty) {
        var findIt = false;
        for (var i = 0; i < node.children.length; i++) {
          if (node.children[i].data.id == selectNode?.node.data.id) {
            findIt = true;
            break;
          }
        }
        if (findIt) {
          _addChild(node);
          return true;
        } else {
          for (var i = 0; i < node.children.length; i++) {
            var flag = _searchForAddBrotherNode(node.children[i]);
            if (flag) {
              return true;
            }
          }
        }
      }
      return false;
    }
    if (map?.root != null) {
       _searchForAddBrotherNode(map!.root);
    }
   
  }

  void deleteNode() {
    bool _searchForDeleteNode(Node node) {
      if (node.children.isNotEmpty) {
        var findIt = -1;
        for (var i = 0; i < node.children.length; i++) {
          if (node.children[i].data.id == selectNode?.node.data.id) {
            findIt = i;
            break;
          }
        }
        if (findIt != -1) {
          node.children.removeAt(findIt);
          selectNode = null;
          _reloadDataFromJson();
          if (mounted) {
            setState(() {});
          }
          return true;
        } else {
          for (var i = 0; i < node.children.length; i++) {
            var flag = _searchForDeleteNode(node.children[i]);
            if (flag) {
              return true;
            }
          }
        }
      }
      return false;
    }

    O2Dialogs.showConfirmDialog(
            context: context, message: '确定要删除当前主题，会同时删除它的所有下级主题？')
        .then((action) {
      if (action == O2DialogAction.positive && map?.root != null) {
        _searchForDeleteNode(map!.root);
      }
    });
  }

  ///
  /// 编辑超链接 对话框
  ///
  void _showLinkDialog() async {
    final link = selectNode?.node.data.hyperlink ?? '';
    final linkTitle = selectNode?.node.data.hyperlinkTitle ?? '';
    final result = await Navigator.push(
        context,
        MaterialPageRoute(
          builder: (BuildContext context) => MindMapHyperlinkForm(
            hyperlink: link,
            hyperlinkTitle: linkTitle,
          ),
          fullscreenDialog: true,
        ));
    if (result != null) {
      _updateSelectData((result as NodeData));
    }
  }

  ///
  /// 弹出popWindow 显示工具： 优先级、进度
  void _showIconTools() {
    bottomSheetController = _scaffoldKey.currentState?.showBottomSheet(
//    bottomSheetController = showBottomSheet(
//        context: context,
//        builder:
        (build) {
      final height = mediaSize!.height / 2;
      var priorityList = <Widget>[];
      for (var i = 0; i < 10; i++) {
        final path = i == 0 ? 'images/priorityx.png' : 'images/priority$i.png';
        priorityList.add(GestureDetector(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(10, 0, 0, 0),
            child: Image.asset(path),
          ),
          onTap: () {
            _tapProgressOrPriority('priority', i);
          },
        ));
      }
      var progressList = <Widget>[];
      for (var i = 0; i < 10; i++) {
        final path = i == 0 ? 'images/progressx.png' : 'images/progress$i.png';
        progressList.add(GestureDetector(
          child: Padding(
            padding: const EdgeInsets.fromLTRB(10, 0, 0, 0),
            child: Image.asset(path),
          ),
          onTap: () {
            _tapProgressOrPriority('progress', i);
          },
        ));
      }
      return Container(
          height: height,
          child: SingleChildScrollView(
            scrollDirection: Axis.vertical,
            child: Column(
              mainAxisSize: MainAxisSize.max,
              children: <Widget>[
                Container(
                  height: 1,
                  color: Colors.black12,
                ),
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 10, 20, 20),
                  child: Container(
                    height: 8,
                    width: 88,
                    decoration: BoxDecoration(
                        color: Colors.grey[300],
                        borderRadius: const BorderRadius.all(Radius.circular(8))),
                  ),
                ),
                const Padding(
                  padding: EdgeInsets.fromLTRB(20, 0, 20, 20),
                  child: Text(
                    '图标',
                    style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
                  ),
                ),
                const Align(
                  alignment: Alignment.centerLeft,
                  child: Padding(
                    padding: EdgeInsets.fromLTRB(20, 0, 20, 10),
                    child: Text(
                      '优先级：',
                      style: TextStyle(fontSize: 16.0, color: Colors.black45),
                    ),
                  ),
                ),
                Padding(
                  padding: EdgeInsets.fromLTRB(20, 0, 20, 20),
                  child: Wrap(
                    spacing: 5,
                    runSpacing: 10,
                    children: priorityList,
                  ),
                ),
                const Align(
                    alignment: Alignment.centerLeft,
                    child: Padding(
                      padding: EdgeInsets.fromLTRB(20, 0, 20, 10),
                      child: Text(
                        '进度：',
                        style: TextStyle(fontSize: 16.0, color: Colors.black45),
                      ),
                    )),
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 0, 20, 20),
                  child: Wrap(
                    spacing: 5,
                    runSpacing: 10,
                    children: progressList,
                  ),
                )
              ],
            ),
          ));
    });
    bottomSheetController?.closed.whenComplete(() {
      bottomSheetController = null;
    });
  }

  void _showImagePickMenu() {
    final imageUrl = selectNode?.node.data.image;
    final imageId = selectNode?.node.data.imageId;
    var hasImage = false;
    if (imageUrl != null && imageUrl.isNotEmpty) {
      hasImage = true;
    }
    if (imageId != null && imageId.isNotEmpty) {
      hasImage = true;
    }
    showModalBottomSheet(
        context: context,
        builder: (context) {
          return Wrap(
            children: <Widget>[
              hasImage
                  ? ListTile(
                      onTap: () {
                        Navigator.of(context).pop();
                        //
                        _clearNodeImage();
                      },
                      leading: Icon(Icons.delete),
                      title: Text('删除图片', style: O2UI.primaryTextStyle),
                    )
                  : Container(
                      height: 2,
                    ),
              ListTile(
                onTap: () {
                  Navigator.of(context).pop();
                  _chooseImageAndUpload(ImagePickerType.gallery);
                },
                leading: Icon(Icons.photo_library),
                title: Text('相册', style: O2UI.primaryTextStyle),
              ),
              ListTile(
                onTap: () {
                  Navigator.of(context).pop();
                  _chooseImageAndUpload(ImagePickerType.camera);
                },
                leading: Icon(Icons.camera),
                title: Text('拍照', style: O2UI.primaryTextStyle),
              ),
              Container(
                color: O2UI.backgroundColor,
                height: 4,
              ),
              ListTile(
                onTap: () {
                  Navigator.of(context).pop();
                },
                title: const Align(
                  alignment: Alignment.center,
                  child: Text('取消', style: O2UI.primaryTextStyle),
                ),
              )
            ],
          );
        });
  }

  Widget _loadingOverlay() {
    return Stack(
      children: [
        const Opacity(
          opacity: 0.5,
          child: ModalBarrier(dismissible: false, color: Colors.black),
        ),
        const Center(
          child:  CircularProgressIndicator(),
        ),
      ],
    );
  }

  void _scaleStart(ScaleStartDetails detail) {
    if (numPointers == 1 && !scaling) {
      lastPosition = canvasPosition; //标记下位置
      startMovePosition = detail.focalPoint;
    }
    if (numPointers == 2) {
      if (!scaling) {
        scaling = true;
      }
    }
  }

  void _scaleUpdate(ScaleUpdateDetails detail) {
    if (numPointers == 1 && !scaling) {
      final distance = detail.focalPoint - startMovePosition!;
      final newPosition = lastPosition! + distance; //移动
      var x = newPosition.dx;
      var minX = 0 - _scaleX(maxNodeX);
      var maxX = mediaSize == null
          ? 0 - _scaleX(minNodeX)
          : 0 - _scaleX(minNodeX) + mediaSize!.width;
      if (x > maxX) {
        x = maxX;
      }
      if (x < minX) {
        x = minX;
      }
      var y = newPosition.dy;
      var minY = 0 - _scaleY(maxNodeY);
      var maxY = mediaSize == null
          ? 0 - _scaleY(minNodeY)
          : 0 - _scaleY(minNodeY) + mediaSize!.height;
      if (y > maxY) {
        y = maxY;
      }
      if (y < minY) {
        y = minY;
      }
      canvasPosition = Offset(x, y); //移动
      setState(() {});
    }
    if (numPointers == 2) {
      if (scaling) {
        var newScale = lastScale * detail.scale;
        if (newScale < 0.2) {
          newScale = 0.2;
        } else if (newScale > 1.0) {
          newScale = 1.0;
        }
        scale = newScale;
        // 计算大小
        setState(() {});
      }
    }
  }

  void _scaleEnd(ScaleEndDetails detail) {
    if (scaling) {
      lastScale = scale;
      scaling = false;
    }
  }

  void _tapUp(BuildContext context, TapUpDetails detail) {
    debugPrint('_tapup.............');
    _closeMoreTool();
    if (autoFocus) {
      _closeEdit(); //点击取消
    } else {
      RenderBox? canvasBox = globalKey.currentContext?.findRenderObject() as RenderBox?;
      Offset? canvasLocal = canvasBox?.globalToLocal(detail.globalPosition);
      debugPrint('canvasLocal.:$canvasLocal');
      if (canvasLocal != null && node != null) {
         selectNode = _checkTapNode(node!, canvasLocal);
      }
      _moveSelectPositionToCenter();
    }
    setState(() {});
  }

  void _moveSelectPositionToCenter() {
    if (selectNode != null) {
      // 居中显示
      var x = 0 - _scaleX(selectNode!.node.offset!.dx);
      var y = 0 - _scaleY(selectNode!.node.offset!.dy);
      x = mediaSize == null
          ? x
          : x +
              mediaSize!.width / 2 -
              (selectNode!.node.nodeSize.width * scale) / 2;
      y = mediaSize == null
          ? y
          : y +
              mediaSize!.height / 2 -
              (selectNode!.node.nodeSize.height * scale) / 2;

//      canvasPosition = Offset(x, y); //移动
      final old = canvasPosition;
      final end = Offset(x, y);
      print("old:$old");
      print('end:$end');
      if (movePositionController!=null) {
        tween = MaterialPointArcTween(begin: old, end: end)
          .animate(movePositionController!);
        movePositionController!.forward(from: 0.0);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////

  // 考虑放大缩小的问题
  double _scaleX(double x) {
    if (scale < 1) {
      return x * scale + (canvasSize!.width - canvasSize!.width * scale) / 2;
    } else {
      return x;
    }
  }

  double _scaleY(double y) {
    if (scale < 1) {
      return y * scale + (canvasSize!.height - canvasSize!.height * scale) / 2;
    } else {
      return y;
    }
  }

  void _closeMoreTool() {
    if (bottomSheetController != null) {
      bottomSheetController!.close();
    }
  }

  ///
  /// 设置 进度 或 优先级
  /// tag ： progress priority
  ///
  void _tapProgressOrPriority(String tag, int result) {
    _closeMoreTool();
    var data = NodeData();
    switch (tag) {
      case 'progress':
        data.progress = result;
        break;
      case 'priority':
        data.priority = result;
        break;
      default:
        break;
    }
    _updateSelectData(data);
  }

  ///
  /// 文字编辑
  ///
  void _editNodeText() {
    debugPrint('编辑文字。。。。。');
    if (selectNode != null) {
      debugPrint('select node is here!!!!!!');
      final selectFill = selectNode!.node.paintElements[NodeElement.background];
      final selectText = selectNode!.node.paintElements[NodeElement.text];
      if (selectText != null && selectText is TextPaintElement) {
        debugPrint('text paint is here !.........');
        inputWidth = selectText.painter.width;
        inputHeight = selectText.painter.height;
        if (selectText.painter.text != null) {
          textSpan = selectText.painter.text as TextSpan;
        }
        inputLeft = selectText.offset?.dx ?? 0;
        inputTop = selectText.offset?.dy ?? 0;
        inputViewIsHidden = false;
        autoFocus = true;
      }
      if (selectFill != null) {
        debugPrint('select node background is here!!.....');
        inputFillColor = (selectFill as RRectPaintElement).style.color;
      } else {
        inputFillColor = map!.mapTheme.canvasBackgroundColor;
      }
      setState(() {});
    }
  }

  ///
  /// 关闭文字编辑
  ///
  void _closeEdit() {
    debugPrint('_closeEdit...........');
    inputWidth = 0.0;
    inputHeight = 0.0;
    textSpan = null;
    inputLeft = 0.0;
    inputTop = 0.0;
    inputViewIsHidden = true;
    autoFocus = false;
  }

  ///
  /// 修改选中的节点的数据
  ///
  void _updateSelectData(NodeData data) {
    debugPrint('修改数据并刷新');
    _closeEdit(); // 关闭编辑
    if (selectNode?.node.data.id == 'root' && map?.root.data != null) {
      _setData(map!.root.data, data);
    } else {
      if (map?.root.children != null && map?.root.children.isNotEmpty == true && selectNode?.node.data != null) {
        for (var child in map!.root.children) {
          _searchForUpdateSelectData(child, data, selectNode!.node.data.id ?? '');
        }
      }
    }
    // reload
    _reloadDataFromJson();
    selectNode = null;
    setState(() {});
  }

  void _searchForUpdateSelectData(Node node, NodeData data, String selectId) {
    if (node.data.id == selectId) {
      _setData(node.data, data);
    } else {
      if (node.children != null && node.children.length > 0) {
        for (var child in node.children) {
          _searchForUpdateSelectData(child, data, selectId);
        }
      }
    }
  }

  void _setData(NodeData oldData, NodeData newData) {
    if (newData.text != null) {
      oldData.text = newData.text;
    }
    if (newData.progress != null) {
      oldData.progress = newData.progress;
    }
    if (newData.priority != null) {
      oldData.priority = newData.priority;
    }
    if (newData.image != null || newData.imageId != null) {
      oldData.image = newData.image;
      oldData.imageId = newData.imageId;
      oldData.imageTitle = newData.imageTitle;
      oldData.imageSize = newData.imageSize;
    }
    if (newData.hyperlink != null) {
      oldData.hyperlink = newData.hyperlink;
      oldData.hyperlinkTitle = newData.hyperlinkTitle;
    }
  }

  //给Node添加子节点
  void _addChild(Node n) {
    int childNum = n.children == null ? 0 : n.children.length;
    childNum++;
    final time = DateTime.now().millisecond;
    final id = 'mind_$time';
    NodeData data = NodeData(id: id, created: time, text: '子主题$childNum');
    Node child = Node(data: data, children: []);
    if (n.children.isNotEmpty) {
      var children = n.children;
      children.add(child);
      n.children = children;
    } else {
      n.children = <Node>[child];
    }
    // reload
    _reloadDataFromJson();
    // selected
    selectNode = _autoSelect(node!, id);
    _moveSelectPositionToCenter();
    // 马上编辑
    _editNodeText();
  }

  //获取Data计算所有的内容
  void _reloadDataFromJson() {
    node = map!.mapTheme.calElementSize(map!.root);
    _cacheImage(node!);
    canvasSize = map!.mapTemplate.canvasSize(node!);
    mediaSize ??= MediaQuery.of(context).size;
    if (mediaSize != null && mediaSize!.width > 0) {
      double newWidth = canvasSize!.width;
      double newHeight = canvasSize!.height;
      if (canvasSize!.width < mediaSize!.width) {
        newWidth = mediaSize!.width;
      }
      if (canvasSize!.height < mediaSize!.height) {
        newHeight = mediaSize!.height;
      }
      canvasSize = Size(newWidth, newHeight);
    }
    linePaintElement = map!.mapTemplate.paintElementPosition(node!, canvasSize!);

    // position计算完成后 获取边界的节点
    void searchEdgeOffsetChild(NodePaintElement child) {
      if (minNodeX > child.offset!.dx) {
        minNodeX = child.offset!.dx;
      }
      if (maxNodeX < child.offset!.dx) {
        maxNodeX = child.offset!.dx;
      }
      if (minNodeY > child.offset!.dy) {
        minNodeY = child.offset!.dy;
      }
      if (maxNodeY < child.offset!.dy) {
        maxNodeY = child.offset!.dy;
      }
      if (child.children != null && child.children!.length > 0) {
        for (var i = 0; i < child.children!.length; i++) {
          searchEdgeOffsetChild(child.children![i]);
        }
      }
    }

    void searchEdgeOffset() {
      //初始化最大最小位置
      var positionX = node?.offset!.dx;
      var positionY = node?.offset!.dy;
      minNodeX = positionX!;
      maxNodeX = positionX;
      minNodeY = positionY!;
      maxNodeY = positionY;
      if (node?.children != null && node?.children!.isNotEmpty == true) {
        for (var i = 0; i < node!.children!.length; i++) {
          searchEdgeOffsetChild(node!.children![i]);
        }
      }
    }

    searchEdgeOffset();
    debugPrint(
        '查询边界结果, minx:$minNodeX , maxx:$maxNodeX ,miny:$minNodeY , maxy:$maxNodeY');
  }

  ///
  /// 选中节点
  ///
  SelectNode? _checkTapNode(NodePaintElement? node, Offset localPosition) {
    if (node != null) {
      final left = node.offset!.dx;
      final top = node.offset!.dy;
      final width = node.nodeSize.width;
      final height = node.nodeSize.height;
      if (left <= localPosition.dx &&
          localPosition.dx <= left + width &&
          top <= localPosition.dy &&
          localPosition.dy <= top + height) {
        Rect rect = Rect.fromLTWH(left, top, width, height);
        RRectPaintElement selectrect = RRectPaintElement(
            RRect.fromRectAndRadius(rect, const Radius.circular(5.0)),
            PaintStyle(
                color: Colors.redAccent,
                style: PaintingStyle.stroke,
                strokeWidth: 2.0));
        return SelectNode(node, selectrect);
      } else {
        if (node.children != null && node.children!.isNotEmpty) {
          for (var child in node.children!) {
            var select = _checkTapNode(child, localPosition);
            if (select != null) {
              return select;
            }
          }
        }
      }
    }
    return null;
  }

  ///
  /// 选中节点
  ///
  SelectNode? _autoSelect(NodePaintElement? node, String id) {
    if (node != null) {
      final left = node.offset!.dx;
      final top = node.offset!.dy;
      final width = node.nodeSize.width;
      final height = node.nodeSize.height;
      if (node.data.id == id) {
        Rect rect = Rect.fromLTWH(left, top, width, height);
        RRectPaintElement selectrect = RRectPaintElement(
            RRect.fromRectAndRadius(rect, Radius.circular(5.0)),
            PaintStyle(
                color: Colors.redAccent,
                style: PaintingStyle.stroke,
                strokeWidth: 2.0));
        return SelectNode(node, selectrect);
      } else {
        if (node.children != null && node.children!.isNotEmpty) {
          for (var child in node.children!) {
            var select = _autoSelect(child, id);
            if (select != null) {
              return select;
            }
          }
        }
      }
    }
    return null;
  }

  void _cacheImage(NodePaintElement node) {
    if (node.data.image != null || node.data.imageId != null) {
      var url = node.data.image;
      if (node.data.imageId != null && node.data.imageId != 'null' && node.data.imageId?.isNotEmpty == true) {
        url = O2ApiManager.instance.getFileURL(node.data.imageId);
      }
      if (url !=null && url != 'null' && url.isNotEmpty) {
        _loadNetworkImage(url);
      }
    }
    if (node.children != null && node.children!.isNotEmpty) {
      for (var child in node.children!) {
        _cacheImage(child);
      }
    }
  }

  ///
  /// 画布中的网络图片异步获取刷新
  ///
  void _loadNetworkImage(String url) {
    ///
    /// 下载网络图片 ，然后更新画布
    ///
    if (!mindMapImages.containsKey(url)) {
      Image.network(url).image.resolve(createLocalImageConfiguration(context))
        .addListener(ImageStreamListener(
            (ImageInfo imageInfo, bool synchronousCall) async {
          var img = imageInfo.image;
          if (mounted) {
            mindMapImages[url] = img;
            setState(() {});
          }
        }));
    }
  }

  // final ImagePicker _picker = ImagePicker();
  
  void _chooseImageAndUpload(ImagePickerType type) async {
    //第一步 选择照片或拍照  
    var backFile = await O2MethodChannelManager.instance.methodChannel.invokeMethod(method_name_o2_pick_image, {
      "source": type.name//gallery,camera 
    });
    // var xfile = await _picker.pickImage(source: source);

    // var file = await ImagePicker.pickImage(
    //     source: source, maxWidth: 400, maxHeight: 400);
    if (backFile != null && backFile is Map) {
      if (backFile.containsKey(param_name_o2_picker_image_file)) {
        var pFile = backFile[param_name_o2_picker_image_file] as String?;
        debugPrintStack(label: "选择图片 $pFile");
        if (pFile != null && pFile.isNotEmpty) {
          //第二步 上传图片 返回id
          Loading.start(context);
          var id = await FileAssembleService()
              .uploadImageForMindMap(_mindMap!.id!, File(pFile), scale: 400);

          if (id != null && id.isNotEmpty) {
            print('上传成功id:$id');
            var url = O2ApiManager.instance.getFileURL(id);
            //更新缓存
            _loadNetworkImage(url);
            //根据node的Image信息
            await _readImageSizeAndUpdateNode(File(pFile), url, id);
            Loading.complete(context);
          } else {
            Loading.complete(context);
            _showErrorMessage('图片上传失败');
          }
        } else {
          debugPrintStack(label: '没有选择图片。pFile为空。。。');
        }
      } else {
        debugPrintStack(label: '没有选择图片。。file为空。。');
     }
    } else {
      debugPrintStack(label: '没有选择图片。。。。');
    }
  }

  Future<Null> _readImageSizeAndUpdateNode(
      File file, String url, String id) async {
    //第三步 获取图片大小
    var imageInfo = imageTool.decodeImage(file.readAsBytesSync());
    if (imageInfo != null) {
       //第四步 更新node数据 imageId image imageSize（最大200）
      var h = imageInfo.height;
      var w = imageInfo.width;
      print('图片宽度：$w, 高度：$h');
      var size = _scaleImageSize(w, h);
      NodeData data = NodeData();
      data.imageId = id;
      data.image = url;
      data.imageSize = size;
      _updateSelectData(data);
    }
   
    return null;
  }

  void _clearNodeImage() {
    NodeData data = NodeData();
    data.imageId = '';
    data.image = '';
    data.imageSize = null;
    _updateSelectData(data);
  }

  ImageSize _scaleImageSize(int width, int height) {
    int newH = height <= 0 ? 200 : height;
    int newW = width <= 0 ? 200 : width;
    if (height > width) {
      if (height > 200) {
        var scale = height / 200;
        newH = 200;
        newW = width ~/ scale;
      }
    } else {
      if (width > 200) {
        var scale = width / 200;
        newH = height ~/ scale;
        newW = 200;
      }
    }
    return ImageSize(width: newW, height: newH);
  }

  void _showErrorMessage(String message) {
    O2SnackBars.showSnackBar(_scaffoldKey, message);
  }

  ///
  /// 画板生成图片
  ///
  Future<ui.Image> _rendered() async {
    ui.PictureRecorder recorder = ui.PictureRecorder();
    Canvas canvas = Canvas(recorder);
    MindMapPainter painter = MindMapPainter(
      root: node!,
      linePaintElement: linePaintElement,
      selectRect: null,
      priorityImages: priorityImages,
      progressImages: progressImages,
      linkIconImage: linkIconImage,
      mindMapImages: mindMapImages,
    );

    painter.paint(canvas, canvasSize!);
    var image = await recorder
        .endRecording()
        .toImage(canvasSize!.width.floor(), canvasSize!.height.floor());
    return image;
  }
}
