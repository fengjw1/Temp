package com.mp3.launcher4.beans;

import com.google.gson.annotations.SerializedName;

/**
 * @author longzj
 */
public class CategoryDetailBean {
    private String description;
    private String id;
    private String image;
    private String rating;
    private String title;
    private String url;
    @SerializedName(value = "back_keycode")
    private int backCode;
    private String type_id;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getBackCode() {
        return backCode;
    }

    public void setBackCode(int backCode) {
        this.backCode = backCode;
    }
    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    @Override
    public int hashCode() {
        int result = getDescription() != null ? getDescription().hashCode() : 0;
        result = 31 * result + (getId() != null ? getId().hashCode() : 0);
        result = 31 * result + (getImage() != null ? getImage().hashCode() : 0);
        result = 31 * result + (getRating() != null ? getRating().hashCode() : 0);
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        result = 31 * result + getBackCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryDetailBean)) return false;

        CategoryDetailBean bean = (CategoryDetailBean) o;

        if (getBackCode() != bean.getBackCode()) return false;
        if (getDescription() != null ? !getDescription().equals(bean.getDescription()) : bean.getDescription() != null)
            return false;
        if (getId() != null ? !getId().equals(bean.getId()) : bean.getId() != null) return false;
        if (getImage() != null ? !getImage().equals(bean.getImage()) : bean.getImage() != null)
            return false;
        if (getRating() != null ? !getRating().equals(bean.getRating()) : bean.getRating() != null)
            return false;
        if (getTitle() != null ? !getTitle().equals(bean.getTitle()) : bean.getTitle() != null)
            return false;
        return getUrl() != null ? getUrl().equals(bean.getUrl()) : bean.getUrl() == null;
    }




}
