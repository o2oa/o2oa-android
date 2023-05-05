package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager


/**
 * Created by fancyLou on 2020-05-25.
 * Copyright © 2020 O2. All rights reserved.
 */

data class InstantMessage(
        var id: String = "",
        var title: String = "",
        var type: String = "",
        var body: String = "",
        var person: String = "",
        var consumerList: ArrayList<String> = ArrayList(),
        var consumed: Boolean = false,
        var createTime: String = "",
        var updateTime: String = ""
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString() ?: "",
            source.readString() ?: "",
            source.readString() ?: "",
            source.readString() ?: "",
            source.readString() ?: "",
            source.createStringArrayList() ?: ArrayList(),
            1 == source.readInt(),
            source.readString() ?: "",
            source.readString() ?: ""
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(title)
        writeString(type)
        writeString(body)
        writeString(person)
        writeStringList(consumerList)
        writeInt((if (consumed) 1 else 0))
        writeString(createTime)
        writeString(updateTime)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<InstantMessage> = object : Parcelable.Creator<InstantMessage> {
            override fun createFromParcel(source: Parcel): InstantMessage = InstantMessage(source)
            override fun newArray(size: Int): Array<InstantMessage?> = arrayOfNulls(size)
        }
    }

    /**
     * 是否 custom 消息
     */
    fun isCustomType(): Boolean {
        return (!TextUtils.isEmpty(type) && type.startsWith("custom"))
    }

    /**
     * custom  消息体
     */
    fun customO2AppMsg() : CustomO2AppMsg? {
        if (isCustomType() && !TextUtils.isEmpty(body)) {
            try {
                val msgBody = O2SDKManager.instance().gson.fromJson(body, CustomO2AppMsgBody::class.java)
                return msgBody.o2AppMsg
            } catch (ignore: Exception) {}
        }
        return null
    }

}


data class CustomO2AppTextMsg(
    var content: String = "",
    var url: String = "",
)
data class CustomO2AppImageMsg(
    var url: String = "",
)
data class CustomO2AppCardMsg(
    var title: String = "",
    var desc: String = "",
    var url: String = "",
)
enum class CustomO2AppMsgType{
    text,
    image,
    textcard,
    unknown
}
data class CustomO2AppMsg(
    var msgtype: String = "", // text image textcard
    var text: CustomO2AppTextMsg?,
    var image: CustomO2AppImageMsg?,
    var textcard: CustomO2AppCardMsg?,
) {
    fun msgType(): CustomO2AppMsgType {
        return when (msgtype) {
            "text" -> CustomO2AppMsgType.text
            "image" -> CustomO2AppMsgType.image
            "textcard" -> CustomO2AppMsgType.textcard
             else -> CustomO2AppMsgType.unknown
        }
    }
}

data class CustomO2AppMsgBody(
    var o2AppMsg: CustomO2AppMsg?
)