


///
///图片大小对象
///
class ImageSize {
  final int width;
  final int height;

  ImageSize({required this.width, required this.height});

  factory ImageSize.fromJson(Map<String, dynamic> json) {
    int w = json['width'] ?? 0;
    int h = json['height'] ?? 0 ;
    return ImageSize(width: w, height: h);
  }

  Map<String, dynamic> _$ImageSizeToJson(ImageSize instance) {
    var map = <String, dynamic>{};
    map['width'] = instance.width;
    map['height'] = instance.height;
    return map;
  }

  Map<String, dynamic> toJson() => _$ImageSizeToJson(this);

}

///
///脑图节点数据对象
///
class NodeData {
  String? id;
  int? created;
  String? text;
  int? priority; //优先级
  int? progress; //进度
  String? image; //图片地址 根据图片id 生成图片地址
  String? imageTitle; //图片标题
  String? imageId;// 图片id 图片存储在file模块
  ImageSize? imageSize; //图片大小
  String? hyperlink; //超链接
  String? hyperlinkTitle; // 超链接标题

  NodeData(
      {this.id,
      this.created,
      this.text,
      this.priority,
      this.progress,
      this.image,
      this.imageId,
      this.imageTitle,
      this.imageSize,
      this.hyperlink,
      this.hyperlinkTitle});

  factory NodeData.fromJson(Map<String, dynamic> json) {
    return NodeData(
      id: json['id'],
      created: json['created'],
      text: json['text'],
      priority: json['priority'],
      progress: json['progress'],
      image: json['image'],
      imageId: json['imageId'],
      imageTitle: json['imageTitle'],
      imageSize: ImageSize.fromJson((json['imageSize'] is String) ? null : json['imageSize']),
      hyperlink: json['hyperlink'],
      hyperlinkTitle: json['hyperlinkTitle'],
    );
  }

  Map<String, dynamic> _$NodeDataToJson(NodeData instance) {
    var map = <String, dynamic>{};
    if(instance.id!=null) {
      map['id'] = instance.id;
    }
    if(instance.created!=null) {
      map['created'] = instance.created;
    }
    if(instance.text!=null) {
      map['text'] = instance.text;
    }
    if(instance.priority!=null) {
      map['priority'] = instance.priority;
    }
    if(instance.progress!=null) {
      map['progress'] = instance.progress;
    }
    if(instance.image!=null) {
      map['image'] = instance.image;
    }
    if(instance.imageId!=null) {
      map['imageId'] = instance.imageId;
    }
    if(instance.imageTitle!=null) {
      map['imageTitle'] = instance.imageTitle;
    }
    if(instance.imageSize!=null ) {
      map['imageSize'] = instance.imageSize?.toJson();
    }
    if(instance.hyperlink!=null) {
      map['hyperlink'] = instance.hyperlink;
    }
    if(instance.hyperlinkTitle!=null) {
      map['hyperlinkTitle'] = instance.hyperlinkTitle;
    }
    return map;
  }


  Map<String, dynamic> toJson() => _$NodeDataToJson(this);
}

///
///脑图节点对象
///
class Node {
  NodeData data;
  List<Node> children;

  Node({required this.data, required this.children});

  factory Node.fromJson(Map<String, dynamic> json) {
    List<Node> nodes = [];
    if(json['children'] != null) {
      var list = json['children'] as List;
      nodes = list.map((i) => Node.fromJson(i)).toList();
    }
    return Node(
      data: NodeData.fromJson(json['data']),
      children: nodes
    );
  }

  Map<String, dynamic> _$NodeToJson(Node instance) {
    var map = <String, dynamic>{};
    map['data'] = instance.data.toJson();
    var list = <Map<String, dynamic>>[];
    for (var child in instance.children) {
      list.add(child.toJson());
    }
    map['children'] = list;
    return map;
  }

  Map<String, dynamic> toJson() => _$NodeToJson(this);

}

///
/// 节点内容的类型
///
enum NodeElement {
  background, // 背景 rrect circle
  border, // rrect(stroke) line
  text,
  progress,
  priority,
  image,
  hyperlink
}


