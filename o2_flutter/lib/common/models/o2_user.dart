

class O2User {
  String? token;
  String? tokenType; //（anonymous cihper manager user ） cipher代表是服务器之间的连接
  String? id;
  String? distinguishedName;
  String? unique;
  String? createTime;
  String? updateTime;
  String? genderType;
  String? pinyin;
  String? pinyinInitial;
  String? name;
  String? employee;
  String? display;
  String? mail;
  String? qq;
  String? weixin;
  String? mobile;
  String? signature;
  String? superior; //汇报关系
  List<String>? roleList;

  O2User.fromJson(Map<String, dynamic> jsonMap) {
    if(jsonMap!=null) {
      token = jsonMap['token'];
      tokenType = jsonMap['tokenType'];
      id = jsonMap['id'];
      distinguishedName = jsonMap['distinguishedName'];
      unique = jsonMap['unique'];
      createTime = jsonMap['createTime'];
      updateTime = jsonMap['updateTime'];
      genderType = jsonMap['genderType'];
      pinyin = jsonMap['pinyin'];
      pinyinInitial = jsonMap['pinyinInitial'];
      name = jsonMap['name'];
      employee = jsonMap['employee'];
      display = jsonMap['display'];
      mail = jsonMap['mail'];
      qq = jsonMap['qq'];
      weixin = jsonMap['weixin'];
      mobile = jsonMap['mobile'];
      signature = jsonMap['signature'];
      superior = jsonMap['superior'];
    }
  }

}