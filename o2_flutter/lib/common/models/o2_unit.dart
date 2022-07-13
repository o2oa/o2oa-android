

class O2Unit {
  String? id; // 标识ID
  String? pinyin;
  String? pinyinInitial;
  String? name; //公司名称
  String? centerHost; //对应服务端host  如 dev.platform.tech
  String? centerContext; //对应的服务端上下文  如 x_program_center
  int? centerPort; //对应的服务器port  如30080
  String? httpProtocol; //http协议 http https

  O2Unit.fromJson(Map<String, dynamic> jsonMap) {
    id = jsonMap['id'];
    pinyin = jsonMap['pinyin'];
    pinyinInitial = jsonMap['pinyinInitial'];
    name = jsonMap['name'];
    centerHost = jsonMap['centerHost'];
    centerContext = jsonMap['centerContext'];
    centerPort = jsonMap['centerPort'];
    httpProtocol = jsonMap['httpProtocol'];
  }

}