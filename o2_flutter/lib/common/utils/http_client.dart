import 'dart:io';
import 'package:dio/dio.dart';
import 'package:o2_flutter/common/models/api_response.dart';
import 'package:o2_flutter/common/utils/o2_user_manager.dart';

import 'o2_api_manager.dart';


class O2Interceptor extends Interceptor {

  @override
  onError(DioError err,
    ErrorInterceptorHandler handler) {
    print('err:${err.response.toString()}');
    return super.onError(err, handler);

  }
}

class HttpClient {

  static final HttpClient instance = HttpClient._internal();

  factory HttpClient() {
    return instance;
  }

  HttpClient._internal() {
    //初始化
    _dio = Dio();
    _dio.interceptors.add(O2Interceptor());
  }

  late Dio _dio;
  Dio get dio {
    return _dio;
  }



  ///
  /// post方法 json
  ///
  Future<ApiResponse> post(String url, Map<String, dynamic> data, {bool needToken = true}) async {
    Response<dynamic>  response = await _dio.post(url, data: data, options: _postJsonOptions(needToken));
    return  ApiResponse.fromJson(response.data);
  }

  ///
  /// get方法 json
  ///
  Future<ApiResponse> get(String url, {bool needToken = true}) async {
    Response<dynamic> response = await _dio.get(url, options: _getJsonOptions(needToken));
    return  ApiResponse.fromJson(response.data);
  }

  ///
  /// put 方法 json
  ///
  Future<ApiResponse> put(String url, Map<String, dynamic> data, {bool needToken = true}) async {
    Response<dynamic>  response = await _dio.put(url, data: data, options: _putJsonOptions(needToken));
    return  ApiResponse.fromJson(response.data);
  }

  ///
  /// delete方法 json
  ///
  Future<ApiResponse> delete(String url, {bool needToken = true}) async {
    Response<dynamic> response = await _dio.delete(url, options: _deleteJsonOptions(needToken));
    return  ApiResponse.fromJson(response.data);
  }

  ///
  /// PUT方法上次文件
  ///
  Future<ApiResponse> putUploadFile(String url, File file) async {
    var fileName = _filename(file);
    var formData = FormData.fromMap({
      'file': await MultipartFile.fromFile(file.path, filename: fileName),
    });
    Response<dynamic> response = await _dio.put(url, data: formData, options: _putJsonOptions(true));
    return  ApiResponse.fromJson(response.data);
  }


  String _filename(File file) {
    if(file.existsSync()) {
      var index = file.path.lastIndexOf(Platform.pathSeparator);
      return file.path.substring(index+1, file.path.length);
    }
    return '';
  }



  Options _deleteJsonOptions(bool needToken) {
    Map<String, String> header = {};
    if (needToken && O2UserManager.instance.o2User != null) {
      header[O2ApiManager.instance.o2TokenName] =
      O2UserManager.instance.o2User?.token == null ? "" : O2UserManager.instance
          .o2User?.token ?? '';
    }
    return Options(
        method: 'DELETE',
        headers: header,
        contentType: ContentType.json.mimeType
    );
  }

  Options _getJsonOptions(bool needToken) {
    Map<String, String> header = {};
    if (needToken && O2UserManager.instance.o2User != null) {
      header[O2ApiManager.instance.o2TokenName] =
      O2UserManager.instance.o2User?.token == null ? "" : O2UserManager.instance
          .o2User?.token ?? '';
    }
    return Options(
        method: 'GET',
        headers: header,
        contentType: ContentType.json.mimeType
    );
  }

  Options _postJsonOptions(bool needToken) {
    Map<String, String> header = {};
    if (needToken && O2UserManager.instance.o2User != null) {
      header[O2ApiManager.instance.o2TokenName] =
      O2UserManager.instance.o2User?.token == null ? "" : O2UserManager.instance
          .o2User?.token ?? '';
    }
    return Options(
      method: 'POST',
      headers: header,
      contentType: ContentType.json.mimeType
    );
  }

  Options _putJsonOptions(bool needToken) {
    Map<String, String> header = {};
    if (needToken && O2UserManager.instance.o2User != null) {
      header[O2ApiManager.instance.o2TokenName] =
      O2UserManager.instance.o2User?.token == null ? "" : O2UserManager.instance
          .o2User?.token ?? '';
    }
    return Options(
        method: 'PUT',
        headers: header,
        contentType: ContentType.json.mimeType
    );
  }



}