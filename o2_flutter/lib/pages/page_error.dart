
import 'package:flutter/material.dart';
import 'package:o2_flutter/common/widgets/system_pop_app_bar.dart';

class O2PageError extends StatelessWidget {
  const O2PageError({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: systemPopAppBar('O2OA'),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: const <Widget>[
            Padding(padding: EdgeInsets.all(16),
            child: Text('非常抱歉，系统错误！'),)
          ],
        ),
      ),
    );
  }
  
}