package com.mp3.launcher4.beans;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author longzj
 */
public class CategoryBean implements Parcelable {
    public static final Creator<CategoryBean> CREATOR = new Creator<CategoryBean>() {
        @Override
        public CategoryBean createFromParcel(Parcel source) {
            return new CategoryBean(source);
        }

        @Override
        public CategoryBean[] newArray(int size) {
            return new CategoryBean[size];
        }
    };
    private int id;
    private String label;
    private String display;
    private String image;

    public CategoryBean() {
    }

    protected CategoryBean(Parcel in) {
        this.id = in.readInt();
        this.label = in.readString();
        this.display = in.readString();
        this.image = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.label);
        dest.writeString(this.display);
        dest.writeString(this.image);
    }

    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + (getLabel() != null ? getLabel().hashCode() : 0);
        result = 31 * result + (getDisplay() != null ? getDisplay().hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryBean)) return false;

        CategoryBean bean = (CategoryBean) o;

        if (getId() != bean.getId()) return false;
        if (getLabel() != null ? !getLabel().equals(bean.getLabel()) : bean.getLabel() != null)
            return false;
        if (getDisplay() != null ? !getDisplay().equals(bean.getDisplay()) : bean.getDisplay() != null)
            return false;
        return image != null ? image.equals(bean.image) : bean.image == null;
    }
}
