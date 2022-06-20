import 'package:flutter/material.dart';

import 'final_widget.dart';


bool loadingStatus = false;
class Loading {

  static void start(BuildContext ctx, {String? text}) {
    if (loadingStatus) {
      return ;
    }
    loadingStatus = true;
     showDialog(context: ctx, builder: (context)
     {
       return Scaffold(
         backgroundColor: Colors.transparent,
         body: Center(
           child: Column(
             mainAxisAlignment: MainAxisAlignment.center,
             children: _list(text),
           ),
         ),
       );
     });
  }

  static List<Widget> _list(String? text) {
    if (text == null || text.isEmpty) {
      return <Widget>[
        const CircularProgressIndicator()
      ];
    }else {
      return <Widget>[
        const CircularProgressIndicator(),
        const SizedBox(
          height: 10,
        ),
        Text(text, style: O2UI.whiteTextStyle,)
      ];
    }
  }

  static void complete(BuildContext ctx) {
    if (loadingStatus) {
      loadingStatus = false;
      Navigator.of(ctx, rootNavigator: true).pop();
    }
  }

}

