package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.tbs;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.util.Log;

import com.tencent.smtt.export.external.TbsCoreSettings;
import com.tencent.smtt.sdk.QbSdk;
import com.tencent.smtt.sdk.TbsDownloader;
import com.tencent.smtt.sdk.TbsListener;

import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2;
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager;
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils.security.SecurityEditor;

import java.util.HashMap;

public class WordReadHelper {

    public static final String TAG = "WordReadHelper";
    private boolean mOnlyWifi = false; // 不是wifi也下载
    private boolean mInit = false;
    private Context mContext;
    private NetworkCallbackImpl networkCallback;

    private WordReadHelper() {
    }
    private static final WordReadHelper INSTANCE = new WordReadHelper();
    public static WordReadHelper getInstance() {
        return INSTANCE;
    }

    public void setOnlyWifiDownload(boolean onlyWifi) {
        mOnlyWifi = onlyWifi;
    }

    public void init(Context context) {
        if (context == null) {
            throw new NullPointerException("init fail");
        }
        mContext = context;
        boolean isInstalled =  O2SDKManager.Companion.instance().prefs().getBoolean(O2.TBS_INSTALL_STATUS, false);
        if (!isInstalled) { // 没有下载成功过！ 需要重置sdk 不然没法继续下载
            QbSdk.reset(mContext);
            resetSdk(context);
        }
        QbSdk.setTbsListener(new TbsListener() {
            @Override
            public void onDownloadFinish(int i) {
                //成功时i为100
                if (i != 100) {
                    //此处存在一种情况，第一次启动app，init不会自动回调，此处额外加一层，判断网络监听器是否为空并作出处理
                    initNetWorkCallBack();
                }
                Log.d(TAG, "load" + i);
                //tbs内核下载完成回调
            }

            @Override
            public void onInstallFinish(int i) {
                mInit = true;
                if (networkCallback != null) {
                    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    connMgr.unregisterNetworkCallback(networkCallback);
                }
                Log.d(TAG, "finish" + i);
                //内核安装完成回调，
                if (mInit) {
                    SecurityEditor editor = O2SDKManager.Companion.instance().prefs().edit();
                    editor.putBoolean(O2.TBS_INSTALL_STATUS, true);
                    editor.commit();
                }
            }

            @Override
            public void onDownloadProgress(int i) {
                //下载进度监听
                Log.d(TAG, "progress" + i);
            }
        });
        QbSdk.initX5Environment(context, new QbSdk.PreInitCallback() {
            @Override
            public void onCoreInitFinished() {
                Log.e(TAG, "加载内核完成");
                //x5内核初始化完成回调接口，此接口回调并表示已经加载起来了x5，有可能特殊情况下x5内核加载失败，切换到系统内核。
            }

            @Override
            public void onViewInitFinished(boolean b) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                //该方法在第一次安装app打开不会回调
                mInit = b;
                Log.e(TAG, "加载内核是否成功:" + b);
                if (!mInit) {
                    initNetWorkCallBack();
                }

                if (!mInit && TbsDownloader.needDownload(context, false) && !TbsDownloader.isDownloading()) {
                    initFinish();
                }
                // 这里回调成功才算真正的内核安装成功，缓存下来
                if (mInit) {
                    SecurityEditor editor = O2SDKManager.Companion.instance().prefs().edit();
                    editor.putBoolean(O2.TBS_INSTALL_STATUS, true);
                    editor.commit();
                }
            }
        });
    }

    private void resetSdk(Context context) {
        // 在调用TBS初始化、创建WebView之前进行如下配置
        HashMap map = new HashMap();
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_SPEEDY_CLASSLOADER, true);
        map.put(TbsCoreSettings.TBS_SETTINGS_USE_DEXLOADER_SERVICE, true);
        QbSdk.initTbsSettings(map);
        QbSdk.setDownloadWithoutWifi(!mOnlyWifi);
        QbSdk.disableAutoCreateX5Webview();
        //强制使用系统内核
        //QbSdk.forceSysWebView();
    }

    public boolean initFinish() {
        boolean isInstalled =  O2SDKManager.Companion.instance().prefs().getBoolean(O2.TBS_INSTALL_STATUS, false);
        if (isInstalled) {
            Log.i(TAG, "X5内核已经下载安装过了！");
            return true;
        }
        if (!mInit && !TbsDownloader.isDownloading()) {
            QbSdk.reset(mContext);
            resetSdk(mContext);
            if (!mOnlyWifi || isWifi(mContext)) {
                TbsDownloader.startDownload(mContext);
            }
        }
        return mInit;
    }

    private boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null
                && info.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }


    public  void initNetWorkCallBack() {
        if (networkCallback == null) {
            networkCallback = new NetworkCallbackImpl();
            NetworkRequest.Builder builder = new NetworkRequest.Builder();
            NetworkRequest request = builder.build();
            ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connMgr != null) {
                connMgr.registerNetworkCallback(request, networkCallback);
            }
        }
    }


    public static class NetworkCallbackImpl extends ConnectivityManager.NetworkCallback {

        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);
            WordReadHelper.getInstance().initFinish();
            Log.d(TAG, "onAvailable: 网络已连接");
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            Log.d(TAG, "onLost: 网络已断开");
        }
    }

}