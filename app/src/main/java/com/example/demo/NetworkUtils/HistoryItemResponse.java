package com.example.demo.NetworkUtils;

import com.google.gson.annotations.SerializedName;

// 单个记录模型
public class HistoryItemResponse {
    @SerializedName("imageid")
    private int imageid;

    @SerializedName("image_base64")
    private String imageBase64;

    @SerializedName("time")
    private String time;

    @SerializedName("location")
    private String location;

    @SerializedName("lat")
    private Double lat;

    @SerializedName("lng")
    private Double lng;

    @SerializedName("reportid")
    private int reportid;

    @SerializedName("predlabel")
    private String predlabel;

    @SerializedName("predclass")
    private String predclass;

    @SerializedName("predscore")
    private String predscore;

    // Getter methods
    public int getImageid() {
        return imageid;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public int getReportid() {
        return reportid;
    }

    public String getPredlabel() {
        return predlabel;
    }

    public String getPredclass() {
        return predclass;
    }

    public String getPredscore() {
        return predscore;
    }
}
