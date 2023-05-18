package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用对象 包含了流程数据
 * Created by FancyLou on 2016/2/23.
 */
public class ApplicationWithProcessData implements Parcelable {
    private String id;
    private String name;
    private String alias;
    private String createTime;
    private String updateTime;
    private String description;
    private String applicationCategory;
    private String icon;
    private String creatorPerson;
    private String lastUpdateTime;
    private String lastUpdatePerson;
    private List<String> availableIdentityList;
    private List<String> availableDepartmentList;
    private List<String> availableCompanyList;
    private List<String> controllerList;

    private List<ProcessInfoData> processList;


    public List<ProcessInfoData> getProcessList() {
        return processList;
    }

    public void setProcessList(List<ProcessInfoData> processList) {
        this.processList = processList;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApplicationCategory() {
        return applicationCategory;
    }

    public void setApplicationCategory(String applicationCategory) {
        this.applicationCategory = applicationCategory;
    }

    public String getCreatorPerson() {
        return creatorPerson;
    }

    public void setCreatorPerson(String creatorPerson) {
        this.creatorPerson = creatorPerson;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public String getLastUpdatePerson() {
        return lastUpdatePerson;
    }

    public void setLastUpdatePerson(String lastUpdatePerson) {
        this.lastUpdatePerson = lastUpdatePerson;
    }

    public List<String> getAvailableIdentityList() {
        return availableIdentityList;
    }

    public void setAvailableIdentityList(List<String> availableIdentityList) {
        this.availableIdentityList = availableIdentityList;
    }

    public List<String> getAvailableDepartmentList() {
        return availableDepartmentList;
    }

    public void setAvailableDepartmentList(List<String> availableDepartmentList) {
        this.availableDepartmentList = availableDepartmentList;
    }

    public List<String> getAvailableCompanyList() {
        return availableCompanyList;
    }

    public void setAvailableCompanyList(List<String> availableCompanyList) {
        this.availableCompanyList = availableCompanyList;
    }

    public List<String> getControllerList() {
        return controllerList;
    }

    public void setControllerList(List<String> controllerList) {
        this.controllerList = controllerList;
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
        dest.writeString(this.createTime);
        dest.writeString(this.updateTime);
        dest.writeString(this.description);
        dest.writeString(this.applicationCategory);
        dest.writeString(this.icon);
        dest.writeString(this.creatorPerson);
        dest.writeString(this.lastUpdateTime);
        dest.writeString(this.lastUpdatePerson);
        dest.writeStringList(this.availableIdentityList);
        dest.writeStringList(this.availableDepartmentList);
        dest.writeStringList(this.availableCompanyList);
        dest.writeStringList(this.controllerList);
        dest.writeList(this.processList);
    }

    public void readFromParcel(Parcel source) {
        this.id = source.readString();
        this.name = source.readString();
        this.alias = source.readString();
        this.createTime = source.readString();
        this.updateTime = source.readString();
        this.description = source.readString();
        this.applicationCategory = source.readString();
        this.icon = source.readString();
        this.creatorPerson = source.readString();
        this.lastUpdateTime = source.readString();
        this.lastUpdatePerson = source.readString();
        this.availableIdentityList = source.createStringArrayList();
        this.availableDepartmentList = source.createStringArrayList();
        this.availableCompanyList = source.createStringArrayList();
        this.controllerList = source.createStringArrayList();
        this.processList = new ArrayList<ProcessInfoData>();
        source.readList(this.processList, ProcessInfoData.class.getClassLoader());
    }

    public ApplicationWithProcessData() {
    }

    protected ApplicationWithProcessData(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.alias = in.readString();
        this.createTime = in.readString();
        this.updateTime = in.readString();
        this.description = in.readString();
        this.applicationCategory = in.readString();
        this.icon = in.readString();
        this.creatorPerson = in.readString();
        this.lastUpdateTime = in.readString();
        this.lastUpdatePerson = in.readString();
        this.availableIdentityList = in.createStringArrayList();
        this.availableDepartmentList = in.createStringArrayList();
        this.availableCompanyList = in.createStringArrayList();
        this.controllerList = in.createStringArrayList();
        this.processList = new ArrayList<ProcessInfoData>();
        in.readList(this.processList, ProcessInfoData.class.getClassLoader());
    }

    public static final Parcelable.Creator<ApplicationWithProcessData> CREATOR = new Parcelable.Creator<ApplicationWithProcessData>() {
        @Override
        public ApplicationWithProcessData createFromParcel(Parcel source) {
            return new ApplicationWithProcessData(source);
        }

        @Override
        public ApplicationWithProcessData[] newArray(int size) {
            return new ApplicationWithProcessData[size];
        }
    };
}
