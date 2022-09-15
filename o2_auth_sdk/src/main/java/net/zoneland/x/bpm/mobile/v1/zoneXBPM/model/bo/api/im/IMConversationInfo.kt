package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.im

import android.os.Parcel
import android.os.Parcelable


class IMConversationInfo(
        var id: String? = null,
        var type: String = "",
        var personList: ArrayList<String> = ArrayList(),
        var title: String = "",
        var adminPerson: String? = null,
        var note: String? = null,
        var unreadNumber: Int = 0,
        var isTop: Boolean = false,
        var createTime : String? = null,
        var updateTime : String? = null,
        var lastMessage: IMMessage? = null

): Parcelable {
         constructor(parcel: Parcel) : this(
                 parcel.readString(),
                 parcel.readString() ?: "",
                 parcel.createStringArrayList() ?: ArrayList(),
                 parcel.readString() ?: "",
                 parcel.readString(),
                 parcel.readString(),
                 parcel.readInt(),
                 parcel.readByte() != 0.toByte(),
                 parcel.readString(),
                 parcel.readString(),
                 IMMessage.createFromParcel(parcel)
         ) {
         }

         override fun writeToParcel(parcel: Parcel, flags: Int) {
                 parcel.writeString(id)
                 parcel.writeString(type)
                 parcel.writeString(title)
                 parcel.writeString(adminPerson)
                 parcel.writeString(note)
                 parcel.writeInt(unreadNumber)
                 parcel.writeByte(if (isTop) 1 else 0)
                 parcel.writeString(createTime)
                 parcel.writeString(updateTime)
         }

         override fun describeContents(): Int {
                 return 0
         }

         companion object CREATOR : Parcelable.Creator<IMConversationInfo> {
                 override fun createFromParcel(parcel: Parcel): IMConversationInfo {
                         return IMConversationInfo(parcel)
                 }

                 override fun newArray(size: Int): Array<IMConversationInfo?> {
                         return arrayOfNulls(size)
                 }
         }
 }