
import 'package:flutter/material.dart';

class O2PageNotFound extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: const Text('O2OA'),
      ),
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