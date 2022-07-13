package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im

import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import net.zoneland.x.bpm.mobile.v1.zoneXBPM.O2SDKManager


data class IMMessage(
        var id: String = "",
        var conversationId: String = "",
        var body: String = "",
        var createPerson: String = "",
        var createTime: String = "",

        //消息发送状态 0正常 1发送中 2发送失败要重试
        var sendStatus: Int = 0
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt()
    ) {
    }

    fun messageBody(): IMMessageBody? {
        if (TextUtils.isEmpty(body)) {
            return null
        }
        return O2SDKManager.instance().gson.fromJson(body, IMMessageBody::class.java)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(conversationId)
        parcel.writeString(body)
        parcel.writeString(createPerson)
        parcel.writeString(createTime)
        parcel.writeInt(sendStatus)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IMMessage> {
        override fun createFromParcel(parcel: Parcel): IMMessage {
            return IMMessage(parcel)
        }

        override fun newArray(size: Int): Array<IMMessage?> {
            return arrayOfNulls(size)
        }
    }
}

enum class MessageType(val key:String) {
    text("text"),
    emoji("emoji"),
    image("image"),
    audio("audio"),
    location("location"),
    file("file"),
    process("process"),
    cms("cms")
}

enum class MessageBody(val body:String) {
    image("[图片]"),
    audio("[语音]"),
    location("[位置]"),
    file("[文件]"),
    process("[流程工作]"),
    cms("[信息文章]")
}