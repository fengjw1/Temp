package com.ktc.ecuador.data.protocal;

public class ItemBean {

    private String id = "noId";
    private String image;
    private String title;
    private String url;
    private int back_keycode;

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

    public int getBack_keycode() {
        return back_keycode;
    }

    public void setBack_keycode(int back_keycode) {
        this.back_keycode = back_keycode;
    }
}
