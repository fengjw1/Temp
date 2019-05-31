package com.mp3.launcher4.networks.responses;

import com.mp3.launcher4.beans.CategoryDetailBean;

import java.util.List;

/**
 * @author longzj
 */
public class CategoryDetailResponse {

    private int categoryId;
    private String hash;
    private int expireDate;
    private List<CategoryDetailBean> list;

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(int expireDate) {
        this.expireDate = expireDate;
    }

    public List<CategoryDetailBean> getList() {
        return list;
    }

    public void setList(List<CategoryDetailBean> list) {
        this.list = list;
    }
}
