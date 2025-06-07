package com.example.demo.NetworkUtils;

import com.google.gson.annotations.SerializedName;
import java.util.List;

// 分页响应模型
public class PaginatedResponse {
    @SerializedName("data")
    private List<HistoryItemResponse> data = null;

    @SerializedName("pagination")
    private Pagination pagination = null;

    public List<HistoryItemResponse> getData() {
        return data;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
