package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils


import android.content.Context
import android.util.Log
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.ApiErrorResponse
import retrofit2.adapter.rxjava.HttpException
import rx.functions.Action1
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeoutException

/**
 * Created by fancy on 2017/5/12.
 */

class ExceptionHandler2(val context: Context?, val isHandler: Boolean = false,
                        val onFailure: (Throwable) -> Unit) : Action1<Throwable> {


    override fun call(t: Throwable) {
        if (isHandler) {
            when (t) {
                is TimeoutException -> showConnectionErrorMessage()
                is SocketTimeoutException -> showConnectionErrorMessage()
                is ConnectException -> showConnectionErrorMessage()
                is HttpException -> showO2ErrorMessage(t)
                else -> XLog.error("ExceptionHandler2", t)
            }
        }
        onFailure(t)
    }

    private fun showO2ErrorMessage(exception:HttpException) {
        val buffer = StringBuffer()
        buffer.append("响应代码:"+exception.code())
        try {
            val json = exception.response().errorBody()?.string()
            Log.e("ExceptionHandler","http error body:$json")
            val errorResponse = O2SDKManager.instance().gson.fromJson(json, ApiErrorResponse::class.java)
            if (errorResponse!=null) {
                buffer.append(", 反馈信息:"+errorResponse.message)
            }
        }catch (e1:Exception){
            XLog.error("ExceptionHandler2", e1)
        }
        if (context!=null){
            XToast.toastShort(context, buffer.toString())
        }

    }

    private fun showConnectionErrorMessage() {
        if (context!=null) {
            XToast.toastShort(context, "网络连接异常，请检查您的网络连接是否正确！")
        }
    }
}