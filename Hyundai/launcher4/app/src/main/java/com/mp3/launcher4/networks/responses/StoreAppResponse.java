package com.mp3.launcher4.networks.responses;

import com.mp3.launcher4.beans.AppDetailBean;

import java.util.List;

/**
 * @author longzj
 */
public class StoreAppResponse {

    private int categoryId;
    private String hash;
    private int expireDate;
    private List<AppDetailBean> list;

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

    public List<AppDetailBean> getList() {
        return list;
    }

    public void setList(List<AppDetailBean> list) {
        this.list = list;
    }

}
