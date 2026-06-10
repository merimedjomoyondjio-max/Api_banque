package com.example.bankapi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PagedResponse<T> {

    private List<T> data;
    private int currentPage;
    private int itemsPerPage;
    private long totalItems;
    private int totalPages;
    private boolean isLast;

    public PagedResponse(List<T> data, int currentPage, int itemsPerPage, long totalItems, int totalPages, boolean isLast) {
        this.data = data;
        this.currentPage = currentPage;
        this.itemsPerPage = itemsPerPage;
        this.totalItems = totalItems;
        this.totalPages = totalPages;
        this.isLast = isLast;
    }

    public List<T> getData() {
        return data;
    }

    @JsonProperty("results")
    public List<T> getResults() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    public long getTotalItems() {
        return totalItems;
    }

    @JsonProperty("totalElements")
    public long getTotalElements() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public boolean isLast() {
        return isLast;
    }

    @JsonProperty("isLast")
    public boolean getIsLast() {
        return isLast;
    }

    @JsonProperty("pageSize")
    public int getPageSize() {
        return itemsPerPage;
    }

    public void setLast(boolean last) {
        isLast = last;
    }
}
