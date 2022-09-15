 
import 'dart:convert' show json;

import 'package:flutter/material.dart';
import 'package:o2_flutter/common/models/o2_api_module.dart';
import 'package:o2_flutter/common/models/o2_server.dart';
import 'package:o2_flutter/common/models/o2_unit.dart';
import 'package:o2_flutter/common/utils/shared_preference_manager.dart';

class O2ApiManager {

  static final O2ApiManager instance = O2ApiManager._internal();
  factory O2ApiManager() => instance;
  O2ApiManager._internal();

  O2Unit? _o2unit;
  CenterServerInfo? _centerServerInfo;
  ///
  /// get
  ///
  O2Unit? get o2Unit {
    return _o2unit;
  }
 CenterServerInfo? get centerServerInfo {
    return _centerServerInfo;
 }

 String get o2TokenName {
   if (_centerServerInfo == null) {
     return 'x-token';
   } else {
     return _centerServerInfo?.tokenName ?? 'x-token';
   }
 } 

  ///
  /// 初始化当前连接的O2服务器信息
  ///
  Future<void> initO2Unit(String unitJsonStr) async {
    debugPrintStack(label: 'initO2Unit:$unitJsonStr');
    String? unit = await SharedPreferenceManager.instance.putString(SharedPreferenceManager.FLUTTER_O2_UNIT_KEY, unitJsonStr);
    if (unit != null) {
      _o2unit = O2Unit.fromJson(json.decode(unit));
    }
  }

  ///
  /// 初始化当前连接的O2 center服务器数据
  /// @param centerServerJson
  ///
  Future<void>  initO2CenterServer(String centerServerJson) async {
    debugPrintStack(label: 'initO2WebServer:$centerServerJson');
    String? center = await SharedPreferenceManager.instance.putString(SharedPreferenceManager.FLUTTER_O2_CENTER_SERVER_KEY, centerServerJson);
    if (center != null) {
      _centerServerInfo = CenterServerInfo.fromJson(json.decode(center));
    }
  }


  ///
  /// 文件地址
  ///
  String getFileURL(String? fileId) {
    //http://dev.o2oa.net:20020/x_file_assemble_control/jaxrs/file/b871a896-93f7-4245-8e5a-100fd4a67d9d/download/stream
    String? baseUrl = getModuleBaseUrl(O2DistributeModuleEnum.x_file_assemble_control);
    if(fileId != null && fileId != 'null' && fileId.isNotEmpty && baseUrl != null && baseUrl.isNotEmpty) {
      return '${baseUrl}jaxrs/file/$fileId/download/stream';
    }else {
      return '';
    }
  }

  ///
  /// 获取中心服务器地址
  ///
  String getCenterBaseUrl() {
    if(_o2unit == null) {
      return '';
    }
    String? context = _o2unit?.centerContext;
    if (context == null) {
      return '';
    }
    if(context.contains('/')) {
      return '${_o2unit?.httpProtocol}://${_o2unit?.centerHost}:${_o2unit?.centerPort}$context/';
    }else {
      return '${_o2unit?.httpProtocol}://${_o2unit?.centerHost}:${_o2unit?.centerPort}/$context/';
    }
  }

  ///
  /// 获取模块的url地址
  ///
  String? getModuleBaseUrl(O2DistributeModuleEnum module) {
    ModuleServerInfo? serverInfo;
    switch (module) {
      case O2DistributeModuleEnum.x_processplatform_assemble_surface_script:
        serverInfo = _centerServerInfo?.assembles?.x_processplatform_assemble_surface_script;
        break;
      case O2DistributeModuleEnum.x_processplatform_assemble_surface_task:
        serverInfo = _centerServerInfo?.assembles?.x_processplatform_assemble_surface_task;
        break;
      case O2DistributeModuleEnum.x_processplatform_assemble_surface_worklog:
        serverInfo = _centerServerInfo?.assembles?.x_processplatform_assemble_surface_worklog;
        break;
      case O2DistributeModuleEnum.x_processplatform_assemble_surface_attachment:
        serverInfo = _centerServerInfo?.assembles?.x_processplatform_assemble_surface_attachment;
        break;
      case O2DistributeModuleEnum
          .x_processplatform_assemble_surface_workcompleted:
        serverInfo =
            _centerServerInfo?.assembles?.x_processplatform_assemble_surface_workcompleted;
        break;
      case O2DistributeModuleEnum.x_processplatform_assemble_surface_work:
        serverInfo = _centerServerInfo?.assembles?.x_processplatform_assemble_surface_work;
        break;
      case O2DistributeModuleEnum.x_file_assemble_control:
        serverInfo = _centerServerInfo?.assembles?.x_file_assemble_control;
        break;
      case O2DistributeModuleEnum.x_meeting_assemble_control:
        serverInfo = _centerServerInfo?.assembles?.x_meeting_assemble_control;
        break;
      case O2DistributeModuleEnum.x_attendance_assemble_control:
        serverInfo = _centerServerInfo?.assembles?.x_attendance_assemble_control;
        break;
      case O2DistributeModuleEnum.x_okr_assemble_control:
        serverInfo = _centerServerInfo?.assembles?.x_okr_assemble_control;
        break;
      case O2DistributeModuleEnum.x_bbs_assemble_control:
        serverInfo = _centerServerInfo?.assembles?.x_bbs_assemble_control;
        break;
      case O2DistributeModuleEnum.x_hotpic_assemble_control:
        serverInfo = _centerServerInfo?.assembles?.x_hotpic_assemble_control;
        break;
      case O2DistributeModuleEnum
          .x_processplatform_assemble_surface_applicationdict:
        serverInfo =
            _centerServerInfo?.assembles?.x_processplatform_assemble_surface_applicationdict;
        break;
      case O2DistributeModuleEnum.x_cms_assemble_control:
        serverInfo = _centerServerInfo?.assembles?.x_cms_assemble_control;
        break;
      case O2DistributeModuleEnum.x_organization_assemble_control:
        serverInfo = _centerServerInfo?.assembles?.x_organization_assemble_control;
        break;
      case O2DistributeModuleEnum.x_organization_assemble_custom:
        serverInfo = _centerServerInfo?.assembles?.x_organization_assemble_custom;
        break;
      case O2DistributeModuleEnum.x_processplatform_assemble_surface:
        serverInfo = _centerServerInfo?.assembles?.x_processplatform_assemble_surface;
        break;
      case O2DistributeModuleEnum.x_processplatform_assemble_surface_read:
        serverInfo = _centerServerInfo?.assembles?.x_processplatform_assemble_surface_read;
        break;
      case O2DistributeModuleEnum
          .x_processplatform_assemble_surface_readcompleted:
        serverInfo =
            _centerServerInfo?.assembles?.x_processplatform_assemble_surface_readcompleted;
        break;
      case O2DistributeModuleEnum.x_organization_assemble_express:
        serverInfo = _centerServerInfo?.assembles?.x_organization_assemble_express;
        break;
      case O2DistributeModuleEnum.x_organization_assemble_personal:
        serverInfo = _centerServerInfo?.assembles?.x_organization_assemble_personal;
        break;
      case O2DistributeModuleEnum
          .x_processplatform_assemble_surface_taskcompleted:
        serverInfo =
            _centerServerInfo?.assembles?.x_processplatform_assemble_surface_taskcompleted;
        break;
      case O2DistributeModuleEnum.x_processplatform_assemble_surface_process:
        serverInfo = _centerServerInfo?.assembles?.x_processplatform_assemble_surface_process;
        break;
      case O2DistributeModuleEnum.x_component_assemble_control:
        serverInfo = _centerServerInfo?.assembles?.x_component_assemble_control;
        break;
      case O2DistributeModuleEnum
          .x_processplatform_assemble_surface_application:
        serverInfo = _centerServerInfo?.assembles?.x_processplatform_assemble_surface_application;
        break;
      case O2DistributeModuleEnum.x_processplatform_assemble_surface_data:
        serverInfo = _centerServerInfo?.assembles?.x_processplatform_assemble_surface_data;
        break;
      case O2DistributeModuleEnum.x_processplatform_assemble_designer:
        serverInfo = _centerServerInfo?.assembles?.x_processplatform_assemble_designer;
        break;
      case O2DistributeModuleEnum.x_processplatform_assemble_surface_review:
        serverInfo = _centerServerInfo?.assembles?.x_processplatform_assemble_surface_review;
        break;
      case O2DistributeModuleEnum.x_organization_assemble_authentication:
        serverInfo = _centerServerInfo?.assembles?.x_organization_assemble_authentication;
        break;
      case O2DistributeModuleEnum.x_portal_assemble_surface:
        serverInfo = _centerServerInfo?.assembles?.x_portal_assemble_surface;
        break;
      case O2DistributeModuleEnum.x_calendar_assemble_control:
        serverInfo = _centerServerInfo?.assembles?.x_calendar_assemble_control;
        break;
      case O2DistributeModuleEnum.x_mind_assemble_control:
        serverInfo = _centerServerInfo?.assembles?.x_mind_assemble_control;
        break;
      case O2DistributeModuleEnum.x_teamwork_assemble_control:
        serverInfo = _centerServerInfo?.assembles?.x_teamwork_assemble_control;
        break;
    }
    if (serverInfo == null) {
      return null;
    }
    String protocol = _o2unit == null? "http" : _o2unit?.httpProtocol ?? "http";

    return "$protocol://${serverInfo.host}:${serverInfo.port}${serverInfo.context}/";

  }
}
