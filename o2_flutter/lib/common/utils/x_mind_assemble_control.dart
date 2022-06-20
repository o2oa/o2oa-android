
import 'dart:convert' show json;

import 'package:flutter/material.dart';

import '../../o2.dart';
import '../../pages/mind_map/mind_map_data.dart';
import '../models/api_response.dart';
import '../models/mindmap/mind_folder.dart';
import '../models/mindmap/mind_map.dart';
import '../models/o2_api_module.dart';
import 'http_client.dart';
import 'o2_api_manager.dart';
import 'o2_http_error.dart';

class MindMapService {
  String baseUrl() {
    return O2ApiManager.instance
        .getModuleBaseUrl(O2DistributeModuleEnum.x_mind_assemble_control) ?? '';
  }

  ///
  /// 我的文件
  ///
  Future<List<MindFolder>> myFolderTree() async {
    ApiResponse response =
        await HttpClient.instance.get('${baseUrl()}jaxrs/folder/tree/my');
    if (response.type == o2_http_success) {
      var list = response.data == null ? [] : response.data as List;
      return list.map((folder) => MindFolder.fromJson(folder)).toList();
    } else {
      throw O2HttpError(message: response.message ?? '获取文件夹失败');
    }
  }

  ///
  /// 获取脑图详细信息
  ///
  Future<MindMap> mindMap(String id) async {
    ApiResponse response = await HttpClient.instance.get('${baseUrl()}jaxrs/mind/view/$id');
    if (response.type == o2_http_success) {
      return MindMap.fromJson(response.data);
    }else {
      throw O2HttpError(message: response.message ?? '获取脑图失败');
    }
  }

  ///
  /// 保存脑图数据
  ///
  Future<String> saveMindMap(MindMap? map, MindMapData? data) async {
    if(map == null || data == null) {
      throw O2ValidateError('无法保存，传入参数异常！');
    }
    String content = json.encode(data.toJson());
    map.content = content;
//    map.fileVersion = map.fileVersion + 1 ;//不需要后台已经加过了
    ApiResponse response = await HttpClient.instance.post('${baseUrl()}jaxrs/mind/save', map.toJson());
    if (response.type == o2_http_success) {
      String id = response.data['id'];
      debugPrintStack(label: '保存脑图成功：$id');
      return id;
    } else {
      throw O2HttpError(message: response.message ?? '保存脑图失败');
    }
  }
  ///
  /// 保存脑图数据
  ///
  Future<String> renameMindMap(MindMap? map) async {
    if(map == null ) {
      throw O2ValidateError('无法保存，传入参数异常！');
    }
    ApiResponse response = await HttpClient.instance.post('${baseUrl()}jaxrs/mind/save', map.toJson());
    if (response.type == o2_http_success) {
      String id = response.data['id'];
      return id;
    } else {
      throw O2HttpError(message: response.message ?? '保存脑图失败');
    }
  }

  ///
  /// 删除脑图文件
  ///
  Future<bool> deleteMindMap(String mapId) async {
    ApiResponse response = await HttpClient.instance.delete('${baseUrl()}jaxrs/mind/recycle/$mapId');
    if (response.type == o2_http_success) {
      return true;
    }else{
      throw O2HttpError(message: response.message ?? '删除脑图失败');
    }
  }

  ///
  /// 分页查询脑图列表
  /// @param lastId 上一页最后一个id
  /// @param folderId 所属文件夹
  ///
  Future<List<MindMap>> mindFilterByPage(String lastId, String folderId) async {
    Map<String, String> data = {};
    data['folderId'] = folderId;
    ApiResponse response = await HttpClient.instance.put(
        '${baseUrl()}jaxrs/mind/filter/list/$lastId/next/$default_page_size',
        data);
    if (response.type == o2_http_success) {
      var list = response.data == null ? [] : response.data as List;
      return list.map((folder) => MindMap.fromJson(folder)).toList();
    } else {
      throw O2HttpError(message: response.message ?? '获取脑图列表失败');
    }
  }

  ///
  /// 新增修改目录
  ///
  Future<String> saveMindFolder(String name, String parentId, {String? id}) async {
    Map<String, String> data = Map();
    data['name'] = name;
    data['parentId'] = parentId;
    if (id != null) {
      data['id'] = id;
    }
    ApiResponse response =
        await HttpClient.instance.post('${baseUrl()}jaxrs/folder/save', data);
    if (response.type == o2_http_success) {
      String id = response.data['id'];
      debugPrintStack(label: '保存目录成功：$id');
      return id;
    } else {
      throw O2HttpError(message: response.message ?? '保存目录失败');
    }
  }

  ///
  /// 删除目录
  ///
  Future<bool> deleteMindFolder(String id) async {
    ApiResponse response =
    await HttpClient.instance.delete('${baseUrl()}jaxrs/folder/$id');
    if (response.type == o2_http_success) {
      return true;
    }else {
      throw O2HttpError(message: response.message ?? '删除目录失败');
    }
  }
}
