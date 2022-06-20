import 'package:flutter/material.dart';
import 'package:flutter_slidable/flutter_slidable.dart';
import 'package:o2_flutter/common/routers/application.dart';
import '../../common/models/mindmap/mind_folder.dart';
import '../../common/models/mindmap/mind_map.dart';
import '../../common/models/mindmap/mind_node.dart';
import '../../common/utils/o2_api_manager.dart';
import '../../common/utils/x_mind_assemble_control.dart';
import '../../common/widgets/bottom_sheet_helper.dart';
import '../../common/widgets/dialogs.dart';
import '../../common/widgets/final_widget.dart';
import '../../common/widgets/loading.dart';
import '../../common/widgets/mind_map_end_float_location.dart';
import '../../common/widgets/snack_bars.dart';
import '../../common/widgets/system_pop_app_bar.dart';
import '../../o2.dart';
import 'mind_map_data.dart';


class MindMapHomePage extends StatefulWidget {
  const MindMapHomePage({Key? key}) : super(key: key);

  @override
  _MindMapHomePageState createState() => _MindMapHomePageState();
}

class _MindMapHomePageState extends State<MindMapHomePage>
    with SingleTickerProviderStateMixin {
  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey();
  ScrollController _scrollController = ScrollController(); //listview的控制器
  TextEditingController? newFolderEditingController;
  AnimationController? animationController;
  Animation<double>? animation;
  List<MindMap> _datas = [];
  List<MindFolder> _trees = [];
  bool _isFirstLoading = true;
  String _folderName = '根目录';
  String _folderId = 'root';
  String _lastPageId = firstPageId;

  void _getFolderTree() {
    _trees.clear();
    MindMapService().myFolderTree().then((list) {
      //查询到tree转化成list
      Map<String, dynamic> json = Map();
      json['name'] = '根目录';
      json['id'] = 'root';
      MindFolder root = MindFolder.fromJson(json);
      root.level = 1;
      _trees.add(root);
      _recursionTree(list, 2);
      // 刷新选中目录的名称
      _trees.forEach((tree) {
        if (tree.id == _folderId) {
          _folderName = tree.name ?? '根目录';
        }
      });
      setState(() {});
    }).catchError((error) {
      print(error);
      setState(() {});
    });
  }

  void _recursionTree(List<MindFolder> children, int level) {
    if (children.isEmpty) {
      return;
    }
    for (var tree in children) {
      tree.level = level;
      _trees.add(tree);
      if (tree.children != null && tree.children!.isNotEmpty) {
        _recursionTree(tree.children!, level + 1);
      }
    }
  }

  void _getData() {
    _lastPageId = firstPageId;
    _datas.clear();
    _fetchData();
  }

  void _getMore() {
    if (_datas.isNotEmpty) {
      _lastPageId = _datas.last.id ?? firstPageId;
      _fetchData();
    } else {
      print('没有更多数据了。。。。。。。。。。');
    }
  }

  void _fetchData() {
    MindMapService().mindFilterByPage(_lastPageId, _folderId).then((list) {
      if (list != null && list.isNotEmpty) {
        _datas.addAll(list);
      }
      setState(() {
        _isFirstLoading = false;
      });
    }).catchError((error) {
      print(error);
      setState(() {
        _isFirstLoading = false;
      });
      _showErrorSnap('获取数据异常!');
    });
  }

  void _changeFolder(int index) {
    _folderId = _trees[index].id!;
    _folderName = _trees[index].name!;
    _getData();
  }

  void _newOrEditFolder(String parentId, String? id) {
    var folderName = newFolderEditingController?.text;
    if (folderName == null || folderName.trim().isEmpty) {
      _showErrorSnap('目录名称不能为空！');
    } else {
      Loading.start(context);
      MindMapService().saveMindFolder(folderName, parentId, id: id).then((id) {
        Loading.complete(context);
        _getFolderTree();
      }).catchError((error) {
        print(error);
        Loading.complete(context);
        _showErrorSnap(id == null ? '新建目录失败！' : '修改目录失败！');
      });
    }
  }

  ///
  /// 删除目录
  /// 先判断是否有子目录，然后查询是否有文件 ，全都没有才能删除
  ///
  void _deleteFolderValidate(int index) async {
    MindFolder deleteFolder = _trees[index];
    bool hasSubFolder = true;
    if (index + 1 < _trees.length) {
      MindFolder next = _trees[index + 1];
      if (deleteFolder.level! < next.level!) {
        //是子目录
        _showErrorSnap('无法删除，请先删除当前目录的子目录和文件！');
      } else {
        hasSubFolder = false;
      }
    } else {
      hasSubFolder = false;
    }
    if (!hasSubFolder) {
      List<MindMap> list =
          await MindMapService().mindFilterByPage(firstPageId, deleteFolder.id!);
      if (list.isNotEmpty) {
        _showErrorSnap('无法删除，请先删除当前目录下的所有文件！');
      } else {
        O2Dialogs.showConfirmDialog(message: '确定要删除这个目录？', context: context)
            .then((result) {
          if (result == O2DialogAction.positive) {
            _deleteFolder(index);
          }
        });
      }
    }
  }

  void _deleteFolder(int index) async {
    MindFolder deleteFolder = _trees[index];
    bool result = await MindMapService().deleteMindFolder(deleteFolder.id!);
    if (result) {
      if (_folderId == deleteFolder.id) {
        _changeFolder(0);
      }
      setState(() {
        _trees.removeAt(index);
      });
    }
  }

  ///新建脑图
  void _newMindMap() async {
    var mindName = newFolderEditingController?.text;
    if (mindName == null || mindName.trim().isEmpty) {
      _showErrorSnap('脑图名称不能为空！');
    } else {
      Loading.start(context);
      Node node = Node(data: NodeData(text: mindName), children: []);
      Map<String, dynamic> dataJson = {};
      dataJson['root'] = node.toJson();
      dataJson['template'] = 'default';
      dataJson['theme'] = 'fresh-blue';
      MindMapData data = MindMapData.fromJson(dataJson);
      Map<String, dynamic> mindJson = {};
      mindJson['name'] = mindName;
      mindJson['folderId'] = _folderId;
      mindJson['fileVersion'] = 0;
      MindMap map = MindMap.fromJson(mindJson);
      MindMapService().saveMindMap(map, data).then((id) {
        if (id.isNotEmpty) {
          map.id = id;
          _datas.add(map);
          Loading.complete(context);
          setState(() {
            _gotoMindMapView(_datas.length - 1);
          });
        }
      }).catchError((error) {
        print('新建脑图异常$error');
        Loading.complete(context);
        _showErrorSnap('新建脑图失败！');
      });
    }
  }

  void _renameMindMap(MindMap map) {

    newFolderEditingController?.text = map.name ?? '';

    O2Dialogs.showCustomDialog(
        context: context,
        title: '重命名脑图',
        content: TextField(
          controller: newFolderEditingController,
          autofocus: true,
          decoration:
          const InputDecoration(labelText: '名称', hintText: '请输入脑图名称'),
        )).then((action) {
      if (action == O2DialogAction.positive) {
        _renameMindMap2Remote(map);
      }
    });
  }
  ///新建脑图
  void _renameMindMap2Remote(MindMap map) async {
    var mindName = newFolderEditingController?.text;
    if (mindName == null || mindName.trim().isEmpty) {
      _showErrorSnap('脑图名称不能为空！');
    } else {
      map.name = mindName;
      var allMindMapData = await MindMapService().mindMap(map.id!); //要重新get一下 不然content没有内容
      allMindMapData.name = mindName;
      MindMapService().renameMindMap(allMindMapData).then((id) {
        if (id.isNotEmpty) {

          setState(() {
            print('更新了脑图名称。。。。。。。。');
          });
        }
      }).catchError((error) {
        print('更新脑图异常$error');
        _showErrorSnap('更新脑图失败！');
      });
    }
  }

  void _deleteMindMap(MindMap map) {
    O2Dialogs.showConfirmDialog(
            message: '确定要删除这个脑图，名称：【${map.name}】？', context: context)
        .then((result) {
      if (result == O2DialogAction.positive) {
        MindMapService().deleteMindMap(map.id!).then((result) {
          if (!result) {
            print('删除失败');
          }
          _getData();
        }).catchError((error) {
          print('删除脑图出错，$error');
          _showErrorSnap('删除脑图出错！');
        });
      }
    });
  }

  void _gotoMindMapView(int index) async {
    print('点了第$index行。。。。。。。打开脑图编辑器');
    await AppRouterManager.instance.router?.navigateTo(context, '/mindMap/${_datas[index].id}');
    print('返回了。。。。刷新数据');
    _getData();
  }

  @override
  void initState() {
    super.initState();
    newFolderEditingController = TextEditingController();
    animationController =
        AnimationController(duration: const Duration(milliseconds: 300), vsync: this);
    animation = Tween<double>(begin: 64, end: 400).animate(animationController!)
      ..addListener(() {
        setState(() {});
      });
    _getFolderTree();
    _getData();
    _scrollController.addListener(() {
      if (_scrollController.position.pixels ==
          _scrollController.position.maxScrollExtent) {
        _getMore();
      }
    });
  }

  @override
  void dispose() {
    animationController?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      key: _scaffoldKey,
      appBar: systemPopAppBar('脑图'),
      body: _buildBody(),
      floatingActionButtonLocation: MindMapFloatingActionButtonLocation(),
      floatingActionButton: FloatingActionButton(
        onPressed: _showAddMenu,
        tooltip: '新建',
        child: const Icon(Icons.add),
      ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }

  Widget _buildBody() {
    if (_isFirstLoading) {
      return const Center(child: CircularProgressIndicator());
    } else {
      return Column(
        children: <Widget>[
          Expanded(
            child: _datas.length > 0 ? _gridView() : _emptyDataView(),
          ),
          _bottomFolderBar()
        ],
      );
    }
  }

  //grid 列表
  Widget _gridView() {
    return Padding(
        padding: EdgeInsets.all(5),
        child: GridView.builder(
            controller: _scrollController,
            itemCount: _datas.length,
            gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 2, mainAxisSpacing: 5, crossAxisSpacing: 5),
            itemBuilder: _gridItemView));
  }

  Widget _gridItemView(BuildContext context, int index) {
    MindMap data = _datas[index];
    return InkWell(
      child: GridTile(
        child: Container(
          height: 144,
          color: o2Dark,
          child: FadeInImage.assetNetwork(
              placeholder: 'images/default_image.png',
              image: O2ApiManager.instance.getFileURL(data.icon)),
        ),
        footer: Container(
          height: 40,
          decoration: BoxDecoration(
              border: Border.all(color: O2UI.dividerColor),
              color: Colors.white),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            mainAxisAlignment: MainAxisAlignment.center,
            children: <Widget>[
              Expanded(
                flex: 1,
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(8, 0, 0, 0),
                  child: Text(data.name ?? '', style: O2UI.primaryTextStyle),
                ),
              ),
              IconButton(
                icon: const Icon(Icons.more_vert),
                onPressed: ()=>_showMindMapOperationMenu(data),
              )
            ],
          ),
        ),
      ),
      onTap: () {
        _gotoMindMapView(index);
      },
    );
  }

  //列表
  Widget _listView() {
    return ListView.separated(
        controller: _scrollController,
        itemBuilder: _itemView,
        separatorBuilder: _separatorView,
        itemCount: _datas.length);
  }

  //列表项外框和点击事件
  Widget _itemView(BuildContext context, int index) {
    return InkWell(
      child: _slideRow(index, _datas[index]),
      onTap: () {
        _gotoMindMapView(index);
      },
    );
  }

  //列表项滑动控件
  Widget _slideRow(int index, MindMap data) {
    return Slidable(
      // The end action pane is the one at the right or the bottom side.
        endActionPane: ActionPane(
          motion: const ScrollMotion(),
          children: [
           SlidableAction(
              onPressed: (_) { 
                 _deleteMindMap(_datas[index]);
              },
              backgroundColor: Colors.red,
              foregroundColor: Colors.white,
              icon: Icons.delete,
              label: '删除',
            ),
          ],
        ),
      child: _rowContentView(data),
    );
  }

  //列表项内容
  Widget _rowContentView(MindMap data) {
    return ListTile(
      leading: Container(
        width: 48,
        height: 48,
        color: o2Dark,
        child: FadeInImage.assetNetwork(
          placeholder: 'images/default_image.png',
          image: O2ApiManager.instance.getFileURL(data.icon),
          width: 48,
          height: 48,
        ),
      ),
      title: Text(data.name ?? '', style: O2UI.primaryTextStyle),
      subtitle: Text('版本：${data.fileVersion}', style: O2UI.hintTextStyle),
      trailing:
          Text(_timeFormat(data.updateTime), style: O2UI.hintTextStyle),
    );
  }

  ///
  /// @param time 2019-02-11 12:20:00
  String _timeFormat(String?time) {
    if (time!=null && time.isNotEmpty && time.length > 16) {
      var year = time.substring(0, 4);
      if (DateTime.now().year == int.parse(year)) {
        return time.substring(5, 16);
      } else {
        return time.substring(0, 16);
      }
    } else {
      return "";
    }
  }

  //分割线
  Widget _separatorView(BuildContext context, int index) {
    return O2UI.separatorView;
  }

  // 没有数据的时候显示的文字
  Widget _emptyDataView() {
    return const Center(child: Text('空空如也！', style: O2UI.hintTextStyle));
  }

  // 底部 文件夹 栏
  Widget _bottomFolderBar() {
    var screenWidth = MediaQuery.of(context).size.width;
    return SizedBox(
        height: animation!.value,
        child: Stack(
          children: <Widget>[
            Positioned(
                left: 0,
                top: 0,
                width: screenWidth,
                height: 400,
                child: Column(
                  children: <Widget>[
                    _bottomFolderBarHeader(),
                    Expanded(
                      child: Container(
                        child: _bottomFolderListView(),
                      ),
                    )
                  ],
                ))
          ],
        ));
  }

  Widget _bottomFolderBarHeader() {
    return InkWell(
      onTap: () {
        if (animation!.value > 64) {
          animationController?.reverse();
        } else {
          animationController?.forward();
        }
      },
      child: Container(
        padding: const EdgeInsets.all(16),
        color: O2UI.backgroundColor,
        child: Row(
          children: <Widget>[
            const Icon(Icons.folder, color: O2UI.iconColor, size: 32),
            Expanded(
              child: Align(
                alignment: Alignment.center,
                child: Text(_folderName, style: O2UI.primaryTextStyle),
              ),
            ),
            animation!.value > 64
                ? const Icon(Icons.arrow_drop_down, color: O2UI.iconColor, size: 32)
                : const Icon(Icons.arrow_drop_up, color: O2UI.iconColor, size: 32)
          ],
        ),
      ),
    );
  }

  Widget _bottomFolderListView() {
    return ListView.builder(
      itemBuilder: _folderItemView,
      itemCount: _trees.length,
    );
  }

  Widget _folderItemView(BuildContext context, int index) {
    return InkWell(
      onTap: () {
        _changeFolder(index);
        animationController?.reverse();
      },
      child: Slidable(
        endActionPane: ActionPane(
          motion: ScrollMotion(),
          children: _folderSlideMenu(index),
        ),
        child: ListTile(
          contentPadding:
              EdgeInsets.fromLTRB(16.0 + (_trees[index].level??0 * 8.0), 0, 16, 0),
          title: Text(_trees[index].name ?? ''),
          selected: _trees[index].id == _folderId,
        )
      ),
    );
  }

  ///
  /// 目录列表横拉菜单
  ///
  List<Widget> _folderSlideMenu(int index) {
    if (index == 0) {
      return [];
    } else {
      return <Widget>[
        SlidableAction(
          onPressed: (_) { 
              print('删除目录。。。。。。。。');
            _deleteFolderValidate(index);
          },
          backgroundColor: Colors.red,
          foregroundColor: Colors.white,
          icon: Icons.delete,
          label: '删除',
        ),
        SlidableAction(
          onPressed: (_) { 
              print('重命名目录。。。。。。。。');
            _showFolderDialog(_trees[index]);
          },
          backgroundColor: Colors.blue,
          foregroundColor: Colors.white,
          icon: Icons.edit,
          label: '重命名',
        )
      ];
    }
  }

  ///
  /// 底部弹出菜单 选择 新建脑图 新建文件夹
  ///
  void _showAddMenu() {
    BottomSheetHelper.show(context, <Widget>[
      ListTile(
        onTap: () {
          print('新建脑图。。。。。。。。');
          Navigator.of(context).pop();
          _showNewMindMap();
        },
        leading: const Icon(Icons.add_box),
        title: const Text('新建脑图', style: O2UI.primaryTextStyle),
      ),
      ListTile(
        onTap: () {
          Navigator.of(context).pop();
          _showFolderDialog(null);
        },
        leading: const Icon(Icons.create_new_folder),
        title: const Text('新建目录', style: O2UI.primaryTextStyle),
      )
    ]);
  }

  void _showMindMapOperationMenu(MindMap data) {
    print('显示菜单。。。${data.name}');
    BottomSheetHelper.show(context, <Widget>[
      ListTile(
        onTap: () {
          Navigator.of(context).pop();
          _renameMindMap(data);
        },
        leading: const Icon(Icons.edit),
        title: const Text('重命名', style: O2UI.primaryTextStyle),
      ),
      ListTile(
        onTap: () {
          Navigator.of(context).pop();
          _deleteMindMap(data);
        },
        leading: const Icon(Icons.delete_forever),
        title: const Text('删除', style: O2UI.primaryTextStyle),
      )
    ]);
  }

  void _showErrorSnap(String message) {
    O2SnackBars.showSnackBar(_scaffoldKey, message);
  }

  ///
  /// 新建目录
  ///
  void _showFolderDialog(MindFolder? old) {
    String title;
    String parentId;
    String? id;
    if (old != null) {
      newFolderEditingController?.text = old.name ?? '';
      title = '重命名';
      parentId = old.parentId!;
      id = old.id!;
    } else {
      newFolderEditingController?.text = '';
      title = '在【$_folderName】下新建目录';
      parentId = _folderId;
      id = null;
    }
    O2Dialogs.showCustomDialog(
        context: context,
        title: title,
        content: TextField(
          controller: newFolderEditingController,
          autofocus: true,
          decoration:
              const InputDecoration(labelText: '名称', hintText: '请输入目录名称'),
        )).then((action) {
      if (action == O2DialogAction.positive) {
        _newOrEditFolder(parentId, id);
      }
    });
  }

  ///
  /// 新建脑图Dialog
  ///
  void _showNewMindMap() {
    newFolderEditingController?.text = '';
    O2Dialogs.showCustomDialog(
        context: context,
        title: '在【$_folderName】下新建脑图',
        content: TextField(
          controller: newFolderEditingController,
          autofocus: true,
          decoration:
              const InputDecoration(labelText: '脑图名称', hintText: '请输入脑图名称'),
        )).then((action) {
      if (action == O2DialogAction.positive) {
        _newMindMap();
      }
    });
  }
}
