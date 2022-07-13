

class MindFolder {
  String? id;
  String? name;
  String? parentId;
  int? orderNumber;
  String? description;
  String? creator;
  String? creatorUnit;
  String? createTime;
  String? updateTime;
  String? sequence;
  List<MindFolder>? children;
  int? level;

  
  MindFolder.fromJson(Map<String, dynamic> json) {
      id = json['id'];
      name = json['name'];
      parentId = json['parentId'];
      orderNumber = json['orderNumber'];
      description = json['description'];
      creator = json['creator'];
      creatorUnit = json['creatorUnit'];
      createTime = json['createTime'];
      updateTime = json['updateTime'];
      sequence = json['sequence'];
      var list = json['children'] == null ? [] :  json['children'] as List;
      children = list.map((mind) => MindFolder.fromJson(mind)).toList();
  }

}