import 'package:flutter/material.dart';


typedef MindMapTextEditDone = Function(String text);


class MindMapTextEdit extends StatefulWidget {
  final double inputLeft;
  final double inputTop;
  final double inputWidth;
  final double inputHeight;
  final Color inputFillColor;
  final bool autoFocus;
  final TextSpan? textSpan;
  final MindMapTextEditDone textEditDone;

  const MindMapTextEdit(
      {Key? key,
      required this.inputLeft,
      required this.inputTop,
      required this.inputWidth,
      required this.inputHeight,
      required this.inputFillColor,
      required this.autoFocus,
      required this.textSpan,
      required this.textEditDone})
      : super(key: key);

  @override
  MindMapState createState() {
    return MindMapState();
  }
}

class MindMapState extends State<MindMapTextEdit> {
  late TextEditingController _controller;
  late FocusNode _textNode;
  @override
  void initState() {
    super.initState();
    _controller = TextEditingController();
    _textNode = FocusNode();
  }

  @override
  Widget build(BuildContext context) {
    final text = widget.textSpan?.text ?? ' ';
    _controller.text = text;
    _controller.selection = TextSelection(baseOffset: text.length, extentOffset: text.length);
    if (widget.autoFocus) {
      FocusScope.of(context).requestFocus(_textNode);
    }else {
      FocusScope.of(context).requestFocus(FocusNode());
    }

    return Positioned(
      left: widget.inputLeft,
      top: widget.inputTop,
      width: widget.inputWidth,
      height: widget.inputHeight,
      child: widget.autoFocus ? TextField(
        focusNode: _textNode,
        style: widget.textSpan?.style,
        decoration: InputDecoration.collapsed(
            hintText: '',
            filled: true,
            fillColor: widget.inputFillColor), //去除边框
        controller: _controller,
        textInputAction: TextInputAction.done,
        onEditingComplete: () {
          debugPrint('onEditing complete.....${_controller.text}');
          done();
        },
      ) : Container()
      ,
    );
  }

  @override
  void dispose() {
    _controller.dispose();
    _textNode.dispose();
    super.dispose();
  }

  void done(){
    widget.textEditDone(_controller.text);
  }
}
