
class CenterServerInfo {
  ModuleAssembles? assembles;
  WebServerInfo? webServer;
  String? tokenName; // tokenName是可修改的 x-token是默认值

  CenterServerInfo.fromJson(Map<String, dynamic> jsonMap) {
    if(jsonMap['webServer']!=null) {
      webServer = WebServerInfo.fromJson(jsonMap['webServer']);
    }
    if(jsonMap['assembles']!=null) {
      assembles = ModuleAssembles.fromJson(jsonMap['assembles']);
    }
    if (jsonMap['tokenName'] != null) {
      tokenName = jsonMap['tokenName'];
    } else {
      tokenName = 'x-token';
    }
  }
}

class WebServerInfo {
  String? host;
  int? port;
  String? proxyHost;
  int? proxyPort;
  String? username;
  String? password;
  int? order;
  String? name;

  WebServerInfo.fromJson(Map<String, dynamic> jsonMap) {
    host = jsonMap['host'];
    port = jsonMap['port'];
    proxyHost = jsonMap['proxyHost'];
    proxyPort = jsonMap['proxyPort'];
    username = jsonMap['username'];
    password = jsonMap['password'];
    order = jsonMap['order'];
    name = jsonMap['name'];
  }
}

class ModuleServerInfo {
  String? host;
  int? port;
  String? proxyHost;
  int? proxyPort;
  String? context;

  ModuleServerInfo.fromJson(Map<String, dynamic> jsonMap) {
    host = jsonMap['host'];
    port = jsonMap['port'];
    proxyHost = jsonMap['proxyHost'];
    proxyPort = jsonMap['proxyPort'];
    context = jsonMap['context'];
  }
}

class ModuleAssembles {
  ModuleServerInfo? x_processplatform_assemble_surface_script;
  ModuleServerInfo? x_processplatform_assemble_surface_task;
  ModuleServerInfo? x_processplatform_assemble_surface_worklog;
  ModuleServerInfo? x_processplatform_assemble_surface_workcompleted;
  ModuleServerInfo? x_processplatform_assemble_surface_attachment;
  ModuleServerInfo? x_processplatform_assemble_surface_work;
  ModuleServerInfo? x_file_assemble_control;
  ModuleServerInfo? x_okr_assemble_control;
  ModuleServerInfo? x_bbs_assemble_control;
  ModuleServerInfo? x_hotpic_assemble_control;
  ModuleServerInfo? x_attendance_assemble_control;
  ModuleServerInfo? x_meeting_assemble_control;
  ModuleServerInfo? x_processplatform_assemble_surface_applicationdict;
  ModuleServerInfo? x_cms_assemble_control;
  ModuleServerInfo? x_organization_assemble_control;
  ModuleServerInfo? x_organization_assemble_custom;
  ModuleServerInfo? x_processplatform_assemble_surface;
  ModuleServerInfo? x_processplatform_assemble_surface_read;
  ModuleServerInfo? x_processplatform_assemble_surface_readcompleted;
  ModuleServerInfo? x_organization_assemble_express;
  ModuleServerInfo? x_organization_assemble_personal;
  ModuleServerInfo? x_processplatform_assemble_surface_taskcompleted;
  ModuleServerInfo? x_processplatform_assemble_surface_process;
  ModuleServerInfo? x_component_assemble_control;
  ModuleServerInfo? x_processplatform_assemble_surface_application;
  ModuleServerInfo? x_processplatform_assemble_surface_data;
  ModuleServerInfo? x_processplatform_assemble_designer;
  ModuleServerInfo? x_processplatform_assemble_surface_review;
  ModuleServerInfo? x_organization_assemble_authentication;
  ModuleServerInfo? x_portal_assemble_surface;
  ModuleServerInfo? x_calendar_assemble_control; //日程
  ModuleServerInfo? x_mind_assemble_control; //脑图
  ModuleServerInfo? x_teamwork_assemble_control; //TeamWork

  ModuleAssembles.fromJson(Map<String, dynamic> jsonMap) {
    if (jsonMap['x_processplatform_assemble_surface_script'] != null) {
      x_processplatform_assemble_surface_script = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface_script']);
    }
    if (jsonMap['x_processplatform_assemble_surface_task'] != null) {
      x_processplatform_assemble_surface_task = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface_task']);
    }
    if (jsonMap['x_processplatform_assemble_surface_worklog'] != null) {
      x_processplatform_assemble_surface_worklog = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface_worklog']);
    }
    if (jsonMap['x_processplatform_assemble_surface_workcompleted'] != null) {
      x_processplatform_assemble_surface_workcompleted =
          ModuleServerInfo.fromJson(
              jsonMap['x_processplatform_assemble_surface_workcompleted']);
    }
    if (jsonMap['x_processplatform_assemble_surface_attachment'] != null) {
      x_processplatform_assemble_surface_attachment = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface_attachment']);
    }
    if (jsonMap['x_processplatform_assemble_surface_work'] != null) {
      x_processplatform_assemble_surface_work = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface_work']);
    }
    if (jsonMap['x_file_assemble_control'] != null) {
      x_file_assemble_control =
          ModuleServerInfo.fromJson(jsonMap['x_file_assemble_control']);
    }
    if (jsonMap['x_okr_assemble_control'] != null) {
      x_okr_assemble_control =
          ModuleServerInfo.fromJson(jsonMap['x_okr_assemble_control']);
    }
    if (jsonMap['x_bbs_assemble_control'] != null) {
      x_bbs_assemble_control =
          ModuleServerInfo.fromJson(jsonMap['x_bbs_assemble_control']);
    }
    if (jsonMap['x_hotpic_assemble_control'] != null) {
      x_hotpic_assemble_control =
          ModuleServerInfo.fromJson(jsonMap['x_hotpic_assemble_control']);
    }
    if (jsonMap['x_attendance_assemble_control'] != null) {
      x_attendance_assemble_control =
          ModuleServerInfo.fromJson(jsonMap['x_attendance_assemble_control']);
    }
    if (jsonMap['x_meeting_assemble_control'] != null) {
      x_meeting_assemble_control =
          ModuleServerInfo.fromJson(jsonMap['x_meeting_assemble_control']);
    }
    if (jsonMap['x_processplatform_assemble_surface_applicationdict'] != null) {
      x_processplatform_assemble_surface_applicationdict =
          ModuleServerInfo.fromJson(
              jsonMap['x_processplatform_assemble_surface_applicationdict']);
    }
    if (jsonMap['x_cms_assemble_control'] != null) {
      x_cms_assemble_control =
          ModuleServerInfo.fromJson(jsonMap['x_cms_assemble_control']);
    }
    if (jsonMap['x_organization_assemble_control'] != null) {
      x_organization_assemble_control =
          ModuleServerInfo.fromJson(jsonMap['x_organization_assemble_control']);
    }
    if (jsonMap['x_organization_assemble_custom'] != null) {
      x_organization_assemble_custom =
          ModuleServerInfo.fromJson(jsonMap['x_organization_assemble_custom']);
    }
    if (jsonMap['x_processplatform_assemble_surface'] != null) {
      x_processplatform_assemble_surface = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface']);
    }
    if (jsonMap['x_processplatform_assemble_surface_read'] != null) {
      x_processplatform_assemble_surface_read = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface_read']);
    }
    if (jsonMap['x_processplatform_assemble_surface_readcompleted'] != null) {
      x_processplatform_assemble_surface_readcompleted = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface_readcompleted']);
    }
    if (jsonMap['x_organization_assemble_express'] != null) {
      x_organization_assemble_express = ModuleServerInfo.fromJson(
          jsonMap['x_organization_assemble_express']);
    }
    if (jsonMap['x_organization_assemble_personal'] != null) {
      x_organization_assemble_personal = ModuleServerInfo.fromJson(
          jsonMap['x_organization_assemble_personal']);
    }
    if (jsonMap['x_processplatform_assemble_surface_taskcompleted'] != null) {
      x_processplatform_assemble_surface_taskcompleted = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface_taskcompleted']);
    }
    if (jsonMap['x_processplatform_assemble_surface_process'] != null) {
      x_processplatform_assemble_surface_process = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface_process']);
    }
    if (jsonMap['x_component_assemble_control'] != null) {
      x_component_assemble_control = ModuleServerInfo.fromJson(
          jsonMap['x_component_assemble_control']);
    }
    if (jsonMap['x_processplatform_assemble_surface_application'] != null) {
      x_processplatform_assemble_surface_application = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface_application']);
    }
    if (jsonMap['x_processplatform_assemble_surface_data'] != null) {
      x_processplatform_assemble_surface_data = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface_data']);
    }
    if (jsonMap['x_processplatform_assemble_designer'] != null) {
      x_processplatform_assemble_designer = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_designer']);
    }
    if (jsonMap['x_processplatform_assemble_surface_review'] != null) {
      x_processplatform_assemble_surface_review = ModuleServerInfo.fromJson(
          jsonMap['x_processplatform_assemble_surface_review']);
    }
    if (jsonMap['x_organization_assemble_authentication'] != null) {
      x_organization_assemble_authentication = ModuleServerInfo.fromJson(
          jsonMap['x_organization_assemble_authentication']);
    }
    if (jsonMap['x_portal_assemble_surface'] != null) {
      x_portal_assemble_surface = ModuleServerInfo.fromJson(
          jsonMap['x_portal_assemble_surface']);
    }
    if (jsonMap['x_calendar_assemble_control'] != null) {
      x_calendar_assemble_control = ModuleServerInfo.fromJson(
          jsonMap['x_calendar_assemble_control']);
    }
    if (jsonMap['x_mind_assemble_control'] != null) {
      x_mind_assemble_control = ModuleServerInfo.fromJson(
          jsonMap['x_mind_assemble_control']);
    }
    if (jsonMap['x_teamwork_assemble_control'] != null) {
      x_teamwork_assemble_control = ModuleServerInfo.fromJson(
          jsonMap['x_teamwork_assemble_control']);
    }
  }
}
