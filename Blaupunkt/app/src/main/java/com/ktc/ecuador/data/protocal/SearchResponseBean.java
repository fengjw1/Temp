package com.ktc.ecuador.data.protocal;

import java.util.List;

public class SearchResponseBean {
    private boolean isSuccess = false;
    private String message = "";
    private List<SearchResultApp.ListBean> appResult;
    private List<CategoryDetail.CategoryItem> categoryResult;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<SearchResultApp.ListBean> getAppResult() {
        return appResult;
    }

    public void setAppResult(List<SearchResultApp.ListBean> appResult) {
        this.appResult = appResult;
    }

    public List<CategoryDetail.CategoryItem> getCategoryResult() {
        return categoryResult;
    }

    public void setCategoryResult(List<CategoryDetail.CategoryItem> categoryResult) {
        this.categoryResult = categoryResult;
    }

}
