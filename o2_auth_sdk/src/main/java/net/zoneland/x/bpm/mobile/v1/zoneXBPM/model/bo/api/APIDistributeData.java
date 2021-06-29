package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api;

/**
 * Created by FancyLou on 2016/1/12.
 */
public class APIDistributeData {

    private APIWebServerData webServer;
    private APIAssemblesData assembles;
    private String tokenName; // tokenName是可修改的 x-token是默认值

    // 还有一个mock参数 没有处理
    //"mockConfig": {
    //      "mock": {
    //        "x_processplatform_assemble_surface": {
    //          "put": {
    //            "to": "post",
    //            "append": "mockputtopost"
    //          },
    //          "delete": {
    //            "to": "get",
    //            "append": "mockdeletetoget"
    //          }
    //        },
    //        "x_cms_assemble_control": {
    //          "put": {
    //            "to": "post",
    //            "append": "mockputtopost"
    //          },
    //          "delete": {
    //            "to": "get",
    //            "append": "mockdeletetoget"
    //          }
    //        },
    //        "x_query_assemble_surface": {
    //          "put": {
    //            "to": "post",
    //            "append": "mockputtopost"
    //          },
    //          "delete": {
    //            "to": "get",
    //            "append": "mockdeletetoget"
    //          }
    //        },
    //        "x_organization_assemble_authentication": {
    //          "put": {
    //            "to": "post",
    //            "append": "mockputtopost"
    //          },
    //          "delete": {
    //            "to": "get",
    //            "append": "mockdeletetoget"
    //          }
    //        },
    //        "x_organization_assemble_personal": {
    //          "put": {
    //            "to": "post",
    //            "append": "mockputtopost"
    //          },
    //          "delete": {
    //            "to": "get",
    //            "append": "mockdeletetoget"
    //          }
    //        }
    //      }
    //    }


    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public APIWebServerData getWebServer() {
        return webServer;
    }

    public void setWebServer(APIWebServerData webServer) {
        this.webServer = webServer;
    }

    public APIAssemblesData getAssembles() {
        return assembles;
    }

    public void setAssembles(APIAssemblesData assembles) {
        this.assembles = assembles;
    }
}
