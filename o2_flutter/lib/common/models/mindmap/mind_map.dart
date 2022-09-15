
class MindMap {
  String? id;
  String? name;
  String? folderId;
  String? icon; // 缩略图id 存在文件模块
  int? fileVersion;
  String? creator;
  String? creatorUnit;
  String? createTime;
  String? updateTime;
  bool? shared;
  bool? cooperative;
  String? content;
  bool? editable;
  List<String>? sharePersonList;
  List<String>? shareUnitList;
  List<String>? shareGroupList;
  List<String>? editorList;

  MindMap.fromJson(Map<String, dynamic> json) {
      id = json['id'];
      name = json['name'];
      folderId = json['folderId'];
      icon = json['icon'];
      fileVersion = json['fileVersion'];
      creator = json['creator'];
      creatorUnit = json['creatorUnit'];
      createTime = json['createTime'];
      updateTime = json['updateTime'];
      shared = json['shared'];
      cooperative = json['cooperative'];
      content = json['content'];
      editable = json['editable'];
      var sharePlist = json['sharePersonList'] == null ? [] : json['sharePersonList'] as List;
      sharePersonList = sharePlist.map((share) => share as String).toList();
      var shareUList = json['shareUnitList'] == null ? [] : json['shareUnitList'] as List;
      shareUnitList = shareUList.map((share) => share as String).toList();
      var shareGList = json['shareGroupList'] == null ? [] : json['shareGroupList'] as List;
      shareGroupList = shareGList.map((share) => share as String).toList();
      var shareEList = json['editorList'] == null ? [] : json['editorList'] as List;
      editorList = shareEList.map((share) => share as String).toList();
  }

  Map<String, dynamic> toJson() {
    Map<String, dynamic> map = {};
    map['id'] = id;
    map['name'] = name;
    map['folderId'] = folderId;
    map['icon'] = icon;
    map['fileVersion'] = fileVersion;
    map['creator'] = creator;
    map['creatorUnit'] = creatorUnit;
    map['createTime'] = createTime;
    map['updateTime'] = updateTime;
    map['shared'] = shared;
    map['cooperative'] = cooperative;
    map['content'] = content;
    map['editable'] = editable;
    map['sharePersonList'] = sharePersonList;
    map['shareUnitList'] = shareUnitList;
    map['shareGroupList'] = shareGroupList;
    map['editorList'] = editorList;
    return map;
  }
}