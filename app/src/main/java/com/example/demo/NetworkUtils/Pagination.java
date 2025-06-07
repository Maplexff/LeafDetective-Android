package com.example.demo.NetworkUtils;

import com.google.gson.annotations.SerializedName;

// 分页元数据模型
public class Pagination {
    @SerializedName("current_page")
    private int currentPage = -1;
    @SerializedName("current_record")
    private int currentRecord = -1;
    @SerializedName("page_size")
    private int pageSize = -1;

    @SerializedName("total_records")
    private int totalRecords = -1;

    @SerializedName("total_pages")
    private int totalPages = -1;

    @SerializedName("has_next_page")
    private boolean hasNextPage;

    // Getter methods
    public int getCurrentPage() {
        return currentPage;
    }
    public int getCurrentRecord() {
        return currentRecord;
    }
    public int getPageSize() {
        return pageSize;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }
}
