package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by FancyLou on 2016/2/23.
 */
public class ProcessInfoData implements Parcelable {

    private String id;
    private String name;
    private String alias;
    private String description;

    private String defaultStartMode;	//默认启动方式,draft,instance
    private String startableTerminal;// client,mobile,all 有可能没有值 没有值就是all

    private String edition; // 版本 id
    private String editionName;// 版本名称
    private boolean editionEnable; // 是否启用


    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getEditionName() {
        return editionName;
    }

    public void setEditionName(String editionName) {
        this.editionName = editionName;
    }

    public boolean isEditionEnable() {
        return editionEnable;
    }

    public void setEditionEnable(boolean editionEnable) {
        this.editionEnable = editionEnable;
    }

    public String getStartableTerminal() {
        return startableTerminal;
    }

    public void setStartableTerminal(String startableTerminal) {
        this.startableTerminal = startableTerminal;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDefaultStartMode() {
        return defaultStartMode;
    }

    public void setDefaultStartMode(String defaultStartMode) {
        this.defaultStartMode = defaultStartMode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.alias);
        dest.writeString(this.description);
        dest.writeString(this.defaultStartMode);
        dest.writeString(this.startableTerminal);
        dest.writeString(this.edition);
        dest.writeString(this.editionName);
        dest.writeByte(this.editionEnable ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readString();
        this.name = source.readString();
        this.alias = source.readString();
        this.description = source.readString();
        this.defaultStartMode = source.readString();
        this.startableTerminal = source.readString();
        this.edition = source.readString();
        this.editionName = source.readString();
        this.editionEnable = source.readByte() != 0;
    }

    public ProcessInfoData() {
    }

    protected ProcessInfoData(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.alias = in.readString();
        this.description = in.readString();
        this.defaultStartMode = in.readString();
        this.startableTerminal = in.readString();
        this.edition = in.readString();
        this.editionName = in.readString();
        this.editionEnable = in.readByte() != 0;
    }

    public static final Parcelable.Creator<ProcessInfoData> CREATOR = new Parcelable.Creator<ProcessInfoData>() {
        @Override
        public ProcessInfoData createFromParcel(Parcel source) {
            return new ProcessInfoData(source);
        }

        @Override
        public ProcessInfoData[] newArray(int size) {
            return new ProcessInfoData[size];
        }
    };
}
