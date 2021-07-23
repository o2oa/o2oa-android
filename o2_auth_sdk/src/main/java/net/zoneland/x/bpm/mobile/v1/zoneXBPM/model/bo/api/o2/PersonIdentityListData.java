package net.zoneland.x.bpm.mobile.v1.zoneXBPM.model.bo.api.o2;

import java.util.List;

/**
 * x_organization_assemble_express/jaxrs/identity/list/person
 *
 * Created by FancyLou on 2016/9/10.
 */
public class PersonIdentityListData {

    private List<String> identityList;

    public List<String> getIdentityList() {
        return identityList;
    }

    public void setIdentityList(List<String> identityList) {
        this.identityList = identityList;
    }
}
