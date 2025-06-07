package com.example.demo;
import com.amap.api.maps.model.LatLng;
public class HistoryItem {

    private String time;
    private String location;
    private String reportId;
    private String PredictClass;
    private String PredictScore;
    private String imagebase64;
    private int imageResId;
    private int itemId;
    private LatLng amaplocation;

    public HistoryItem(int itemId,String time, String location, String reportId, int imageResId,String imagebase64,String PredictClass,String PredictScore,LatLng amaplocation) {
        this.itemId = itemId;
        this.time = time;
        this.location = location;
        this.reportId = reportId;
        this.imagebase64 = imagebase64;
        this.imageResId = imageResId;
        this.PredictScore = PredictScore;
        this.PredictClass = PredictClass;
        this.amaplocation = amaplocation;
    }

    public String getTime() {
        return time;
    }

    public String getLocation() {
        return location;
    }


    public String getReportId() {
        return reportId;
    }
    public String getimagebase64() {
        return imagebase64;
    }

    public int getImageResId() {
        return imageResId;
    }
    public int getItemId() {
        return itemId;
    }
    public LatLng getItemLatLng() {
        return amaplocation;
    }
    public String getPredictClass() {
        return PredictClass;
    }
    public String getPredictScore() {
        return PredictScore;
    }
}
