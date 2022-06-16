import 'package:flutter/material.dart';
import 'package:o2_flutter/common/widgets/system_pop_app_bar.dart';

class MindMapHomePage extends StatefulWidget {
  const MindMapHomePage({Key? key}) : super(key: key);

  @override
  _MindMapHomePageState createState() => _MindMapHomePageState();
}

class _MindMapHomePageState extends State<MindMapHomePage> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: systemPopAppBar('脑图'),
      body: _buildBody(),
      // floatingActionButtonLocation: MindMapFloatingActionButtonLocation(),
      // floatingActionButton: FloatingActionButton(
      //   onPressed: _showAddMenu,
      //   tooltip: '新建',
      //   child: Icon(Icons.add),
      // ), // This trailing comma makes auto-formatting nicer for build methods.
    );
  }

  Widget _buildBody() {
    return const Center(
      child: Text('脑图'),
    );
  }
}