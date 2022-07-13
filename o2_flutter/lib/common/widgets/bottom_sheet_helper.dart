import 'package:flutter/material.dart';
import 'final_widget.dart';

class BottomSheetHelper {
  static void show(BuildContext context, List<Widget>? menuList) {
    if (menuList == null || menuList.isEmpty) {
      return;
    }
    List<Widget> list  = [];
    list.addAll(menuList);
    list.add(Container(
      color: O2UI.backgroundColor,
      height: 4,
    ));
    list.add(ListTile(
      onTap: () {Navigator.of(context).pop();},
      title: const Align(
        alignment: Alignment.center,
        child: Text('取消', style: O2UI.primaryTextStyle),
      ),
    ));

    showModalBottomSheet(
        context: context,
        builder: (context) {
          return Wrap(
            children: list,
          );
        });
  }

  static void showWithTopClose(BuildContext context, List<Widget>? menuList,
      {String? title}) {
    if (menuList == null || menuList.isEmpty) {
      return;
    }
    showModalBottomSheet(
        context: context,
        builder: (context) {
          return Column(
            children: <Widget>[
              ListTile(
                title: title != null
                    ? Align(
                  alignment: Alignment.center,
                  child: Text(title, style: O2UI.primaryTextStyle),
                )
                    : null,
                trailing: IconButton(
                    icon: const Icon(Icons.close),
                    onPressed: () {
                      Navigator.of(context).pop();
                    }),
              ),
              Container(
                color: O2UI.backgroundColor,
                height: 4,
              ),
              Expanded(
                child: SingleChildScrollView(
                  child: Wrap(
                    children: menuList,
                  ),
                ),
                flex: 1,
              )
            ],
          );
        });
  }
}
