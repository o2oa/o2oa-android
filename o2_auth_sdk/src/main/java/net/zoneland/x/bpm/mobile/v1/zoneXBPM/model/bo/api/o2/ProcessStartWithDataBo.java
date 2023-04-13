package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2;


import com.google.gson.JsonElement;

/**
 * Created by FancyLou on 2016/2/24.
 */
public class ProcessStartWithDataBo extends ProcessStartBo {

    private JsonElement data;

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }
}


