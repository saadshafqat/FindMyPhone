package com.saarimtech.findmyphone;

public class DeviceListModel {
    String devicename;
    String userKey;
    String lattitude;
    String longitude;

    public DeviceListModel(String devicename, String userKey, String lattitude, String longitude) {
        this.devicename = devicename;
        this.userKey = userKey;
        this.lattitude = lattitude;
        this.longitude = longitude;
    }

    public String getDevicename() {
        return devicename;
    }

    public void setDevicename(String devicename) {
        this.devicename = devicename;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getLattitude() {
        return lattitude;
    }

    public void setLattitude(String lattitude) {
        this.lattitude = lattitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
