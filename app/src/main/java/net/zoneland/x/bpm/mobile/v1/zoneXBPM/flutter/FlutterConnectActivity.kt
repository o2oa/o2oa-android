package net.zoneland.x.bpm.mobile.v1.zoneXBPM.flutter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import io.flutter.embedding.android.FlutterFragment
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.R
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.ImmersedStatusBarUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.XLog


/**
 * Flutter工程连接类
 * flutter的入口
 */
class FlutterConnectActivity : AppCompatActivity(){


    companion object {
        const val ROUTE_NAME_KEY = "ROUTE_NAME_KEY"
        private const val TAG_FLUTTER_FRAGMENT = "flutter_fragment"

        fun startFlutterAppWithRoute(routeName: String): Bundle {
            val bundle = Bundle()
            bundle.putString(ROUTE_NAME_KEY, routeName)
            return bundle
        }
    }

    private var route = "noRoute"



    private var flutterFragment: FlutterConnectFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flutter_connect)
        ImmersedStatusBarUtils.setImmersedStatusBar(this)

        route = intent.extras?.getString(ROUTE_NAME_KEY) ?: "noRoute"
        XLog.debug("open flutter app ， route name : $route")
        val fragmentManager: FragmentManager = supportFragmentManager

        flutterFragment = fragmentManager
            .findFragmentByTag(TAG_FLUTTER_FRAGMENT) as FlutterConnectFragment?

        // Create and attach a FlutterFragment if one does not exist.
        if (flutterFragment == null) {
            flutterFragment = FlutterFragment.NewEngineFragmentBuilder(FlutterConnectFragment::class.java)
                .initialRoute(route)
                .build()
            fragmentManager
                .beginTransaction()
                .replace(
                    R.id.frame_flutter_container,
                    flutterFragment!!,
                    TAG_FLUTTER_FRAGMENT
                )
                .commit()
        }
    }


//    override fun createFlutterView(context: Context?): FlutterView {
//        val matchParent = WindowManager.LayoutParams(-1, -1)
//        val nativeView = this.createFlutterNativeView()
//        val flutterView = FlutterView(this@FlutterConnectActivity, null as AttributeSet?, nativeView)
//        flutterView.setInitialRoute(route)
//        flutterView.layoutParams = matchParent
//        this.setContentView(flutterView)
//        return flutterView
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        route = intent.extras?.getString(ROUTE_NAME_KEY) ?: "noRoute"
//        XLog.debug("open flutter app ， route name : $route")
//        FlutterMain.startInitialization(applicationContext)
//        super.onCreate(savedInstanceState)
//        GeneratedPluginRegistrant.registerWith(this)
//        // 沉浸式状态栏
//        ImmersedStatusBarUtils.setImmersedStatusBar(this)
//        MethodChannel(flutterView, FlutterO2Utils.nativeChannelName, StandardMethodCodec.INSTANCE).setMethodCallHandler { methodCall, result ->
//            when(methodCall.method) {
//                FlutterO2Utils.MethodNameO2Config -> {
//                    val themeSuffix = FancySkinManager.instance().currentSkinSuffix()
//                    XLog.debug("theme:$themeSuffix")
//                    val map = HashMap<String, String>()
//                    if (themeSuffix != "blue") {
//                        map[FlutterO2Utils.parameterNameTheme] = "red"
//                    }else {
//                        map[FlutterO2Utils.parameterNameTheme] = "blue"
//                    }
//                    //user
//                    try {
//                        val user = AuthenticationInfoJson()
//                        user.id = O2SDKManager.instance().cId
//                        user.distinguishedName = O2SDKManager.instance().distinguishedName
//                        user.token = O2SDKManager.instance().zToken
//                        user.name = O2SDKManager.instance().cName
//                        val jsonUser = O2SDKManager.instance().gson.toJson(user)
//                        map[FlutterO2Utils.parameterNameUser] = jsonUser
//                    }catch (e: Exception) {
//                        XLog.error("$e")
//                    }
//                    //unit
//                    try {
//                        val unit = CollectUnitData()
//                        unit.name = O2SDKManager.instance().prefs().getString(O2.PRE_BIND_UNIT_KEY, "")
//                        unit.centerContext = O2SDKManager.instance().prefs().getString(O2.PRE_CENTER_CONTEXT_KEY, "")
//                        unit.centerHost = O2SDKManager.instance().prefs().getString(O2.PRE_CENTER_HOST_KEY, "")
//                        unit.centerPort = O2SDKManager.instance().prefs().getInt(O2.PRE_CENTER_PORT_KEY, 80)
//                        unit.httpProtocol = O2SDKManager.instance().prefs().getString(O2.PRE_CENTER_HTTP_PROTOCOL_KEY, "http")
//                        map[FlutterO2Utils.parameterNameUnit] = O2SDKManager.instance().gson.toJson(unit)
//                    }catch (e: Exception) {
//                        XLog.error("$e")
//                    }
//                    //centerServer
//                    try {
//                        val oldDataJson = O2SDKManager.instance().prefs().getString(O2.PRE_ASSEMBLESJSON_KEY, "")
//                        val oldWebDataJson = O2SDKManager.instance().prefs().getString(O2.PRE_WEBSERVERJSON_KEY, "")
//                        val data = O2SDKManager.instance().gson.fromJson<APIAssemblesData>(oldDataJson, APIAssemblesData::class.java)
//                        val webData = O2SDKManager.instance().gson.fromJson<APIWebServerData>(oldWebDataJson, APIWebServerData::class.java)
//                        val dis = APIDistributeData()
//                        dis.webServer = webData
//                        dis.assembles = data
//                        map[FlutterO2Utils.parameterNameCenterServer] = O2SDKManager.instance().gson.toJson(dis)
//                    }catch (e: Exception) {
//                        XLog.error("$e")
//                    }
//
//                    result.success(map)
//                }
//                else -> {
//                    XLog.error("没有实现当前方法, method:${methodCall.method}")
//                    result.error("没有实现当前方法", "", "")
//                }
//            }
//        }
//    }


}
