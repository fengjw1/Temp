package com.mp3.launcher4.networks.responses;

import com.mp3.launcher4.beans.CategoryBean;

import java.util.List;

/**
 * @author longzj
 */
public class CategoriesResponse {

    private String hash;
    private List<CategoryBean> categories;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<CategoryBean> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryBean> categories) {
        this.categories = categories;
    }

}
