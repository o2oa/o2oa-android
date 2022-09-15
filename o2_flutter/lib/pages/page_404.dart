
import 'package:flutter/material.dart';
import 'package:o2_flutter/common/widgets/system_pop_app_bar.dart';

class O2PageNotFound extends StatelessWidget {
  const O2PageNotFound({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: systemPopAppBar('O2OA'),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Image.asset('images/o2_logo120.png', width: 64, height: 64,),
            const Padding(padding: EdgeInsets.all(16),
            child: Text('非常抱歉，没有找到页面'),)
          ],
        ),
      ),
    );
  }
  
}