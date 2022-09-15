import 'package:flutter/material.dart';
import '../models/mindmap/mind_node.dart';
import 'final_widget.dart';

class MindMapHyperlinkForm extends StatefulWidget {
  final String hyperlink;
  final String hyperlinkTitle;
  MindMapHyperlinkForm({required this.hyperlink, required this.hyperlinkTitle});

  @override
  State<StatefulWidget> createState() {
    return MindMapHyperlinkFormState();
  }
}

class MindMapHyperlinkFormState extends State<MindMapHyperlinkForm> {
  final GlobalKey<FormState> _formKey = GlobalKey();
  late String _link;
  late String _linkTitle;

  @override
  Widget build(BuildContext context) {
    _link = widget.hyperlink;
    _linkTitle = widget.hyperlinkTitle;
    return Scaffold(
        appBar: AppBar(
          title: const Text('链接'),
        ),
        body: Container(
            color: Colors.white,
            width: double.infinity,
            height: double.infinity,
            padding: const EdgeInsets.all(32.0),
            child: SingleChildScrollView(
              child: Form(
                key: _formKey,
                child: Column(children: <Widget>[
                  TextFormField(
                      initialValue: _link,
                      maxLines: 1,
                      autofocus: true,
                      keyboardType: TextInputType.text,
                      decoration: const InputDecoration(
                          labelText: '链接地址',
                          hintText: '必填：以 http(s):// 或 ftp(s):// 开头'),
                      validator: (value) {
                        if (value?.isEmpty == true) {
                          return '链接地址不能为空！';
                        }
                        if (!value!.startsWith(RegExp(r"https?://")) &&
                            !value.startsWith(RegExp(r"ftps?://"))) {
                          return '链接地址格式不正确！';
                        }
                        return null;
                      },
                      onSaved: (value) => _link = value ?? ''),
                  O2UI.divider,
                  TextFormField(
                      initialValue: _linkTitle,
                      maxLines: 1,
                      keyboardType: TextInputType.text,
                      decoration: const InputDecoration(
                          labelText: '链接文本', hintText: '选填'),
                      onSaved: (value) => _linkTitle = value ?? ''),
                  O2UI.divider,
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    children: <Widget>[
                      Expanded(
                        flex: 1,
                        child: Align(
                          alignment: Alignment.center,
                          child: RaisedButton(
                            child: const Text('取消'),
                            onPressed: () {
                              _close(null);
                            },
                          ),
                        ),
                      ),
                      Expanded(
                        flex: 1,
                        child: Align(
                            alignment: Alignment.center,
                            child: RaisedButton(
                              child: const Text('确定'),
                              onPressed: () {
                                if (_formKey.currentState?.validate() == true) {
                                  _formKey.currentState?.save();
                                  NodeData data = NodeData();
                                  data.hyperlink = _link;
                                  data.hyperlinkTitle = _linkTitle;
                                  _close(data);
                                }
                              },
                              color: Colors.blue,
                              textColor: Colors.white,
                            )),
                      ),
                    ],
                  )
                ]),
              ),
            )));
  }

  void _close(NodeData? data) {
    Navigator.pop(context, data);
  }
}
