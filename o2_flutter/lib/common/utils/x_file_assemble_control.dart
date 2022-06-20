import 'dart:io';
import '../../o2.dart';
import '../models/api_response.dart';
import '../models/o2_api_module.dart';
import 'o2_api_manager.dart';
import 'o2_http_error.dart';
import 'http_client.dart';


class FileAssembleService {
  String baseUrl() {
    return O2ApiManager.instance
        .getModuleBaseUrl(O2DistributeModuleEnum.x_file_assemble_control) ?? '';
  }


  ///
  /// 上传图片
  /// 脑图使用的
  ///
  Future<String> uploadImageForMindMap(String mindMapId, File file, {int scale=800}) async {
    ApiResponse response = await HttpClient.instance.putUploadFile('${baseUrl()}jaxrs/file/upload/referencetype/mindInfo/reference/$mindMapId/scale/$scale', file);
    if (response.type == o2_http_success) {
      String id = response.data['id'];
      print('上传脑图缩略图成功：$id');
      return id;
    }else {
      throw O2HttpError(message: response.message ?? '上传脑图缩略图失败');
    }
  }

  ///
  /// 上传图片到Teamwork
  ///
  Future<String> uploadImageForTeamwork(String referenceId, File file, {int scale=800}) async {
    ApiResponse response = await HttpClient.instance.putUploadFile('${baseUrl()}jaxrs/file/upload/referencetype/teamworkProject/reference/$referenceId/scale/$scale', file);
    if (response.type == o2_http_success) {
      String id = response.data['id'];
      print('teamworkProject，上传文件成功：$id');
      return id;
    }else {
      throw O2HttpError(message: response.message ?? '上传文件失败');
    }
  }


}