package com.mp3.launcher4.beans;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * @author longzj
 */
public class AppDetailBean extends RealmObject {

    @PrimaryKey
    private long id;
    @Expose
    @SerializedName(value = "icon-square")
    private String image;
    private String title;
    private String url;
    @Expose
    @SerializedName(value = "back_keyCode")
    private int backCode;

    public AppDetailBean() {

    }

    public AppDetailBean(long id, String image, String title, String url, int backCode) {
        this.id = id;
        this.image = image;
        this.title = title;
        this.url = url;
        this.backCode = backCode;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + (getImage() != null ? getImage().hashCode() : 0);
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getUrl() != null ? getUrl().hashCode() : 0);
        result = 31 * result + getBackCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AppDetailBean)) return false;

        AppDetailBean bean = (AppDetailBean) o;

        if (getId() != bean.getId()) return false;
        if (getBackCode() != bean.getBackCode()) return false;
        if (getImage() != null ? !getImage().equals(bean.getImage()) : bean.getImage() != null)
            return false;
        if (getTitle() != null ? !getTitle().equals(bean.getTitle()) : bean.getTitle() != null)
            return false;
        return getUrl() != null ? getUrl().equals(bean.getUrl()) : bean.getUrl() == null;
    }


    public AppDetailBean renew() {
        return new AppDetailBean(id, image, title, url, backCode);
    }

    public RecentBean createRecentBean() {
        return new RecentBean(id, image, title, url, backCode);
    }
}
