import '../../common/models/mindmap/mind_node.dart';
import 'theme/dark_theme.dart';
import 'theme/mind_map_theme.dart';
import 'template/mind_map_template.dart'; 

class MindMapData {
  final Node root;
  final String template;
  final String theme;
  final String version;

  late BaseTheme mapTheme;
  late BaseTemplate mapTemplate;

  MindMapData(this.root, this.template, this.theme, this.version)  {
    //处理主题
    mapTheme = DarkTheme();
    mapTemplate = DefaultTemplate(mapTheme);
  }


  factory MindMapData.fromJson(Map<String, dynamic> json) {
    return  MindMapData(
        Node.fromJson(json['root']),
        json['template'],
        json['theme'],
        json['version']
    );
  }

  Map<String, dynamic> _$MindMapToJson(MindMapData instance) {
    var map = <String, dynamic>{};
    map['root'] = instance.root.toJson();
    map['template'] = instance.template;
    map['theme'] = instance.theme;
    map['version'] = instance.version;
    return map;
  }

  Map<String, dynamic> toJson() => _$MindMapToJson(this);

}