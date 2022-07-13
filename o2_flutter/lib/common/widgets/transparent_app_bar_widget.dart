import 'package:flutter/material.dart';


class TransparentAppBarWidget extends StatefulWidget {

  final Widget body;
  final Widget title;
  final List<Widget> actions;
  final Color backButtonColor;

  TransparentAppBarWidget({required this.body, required this.title, required this.actions, required this.backButtonColor});

  @override
  State<StatefulWidget> createState() {
    return _TransparentAppBarState();
  }

}

class _TransparentAppBarState extends State<TransparentAppBarWidget> {

  @override
  Widget build(BuildContext context) {
    var padding = MediaQuery.of(context).padding;
    return Stack(
      children: stackChildren(padding),
    );
  }

  List<Widget> stackChildren(EdgeInsets padding) {
    if (widget.body!=null) {
      return <Widget>[
        widget.body,
        Padding(
          padding: padding,
          child: Container(
            height: kToolbarHeight,
            child: Row(
              children: appBarChildren(),
            ),
          ),
        )
      ];
    }else {
      return <Widget>[
        Padding(
          padding: padding,
          child: Container(
            height: kToolbarHeight,
            child: Row(
              children: appBarChildren(),
            ),
          ),
        )
      ];
    }
  }


  List<Widget> appBarChildren() {
    if (widget.actions != null && widget.actions.length > 0) {
      return <Widget>[
        Padding(
          padding: EdgeInsets.fromLTRB(5, 3, 5, 3),
          child: backAndTitle(),
        ),
        Expanded(
          flex: 1,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: widget.actions,
          ),
        )
      ];
    }else {
      return <Widget>[
        Padding(
          padding: EdgeInsets.fromLTRB(5, 3, 5, 3),
          child: backAndTitle(),
        )
      ];
    }
  }

  Widget backAndTitle() {
    if (widget.title!= null) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          BackButton(
            color: widget.backButtonColor == null? Colors.white : widget.backButtonColor,
          ),
          widget.title
        ],
      );
    }else {
      return BackButton(
        color: widget.backButtonColor == null? Colors.white : widget.backButtonColor,
      );
    }
  }

}