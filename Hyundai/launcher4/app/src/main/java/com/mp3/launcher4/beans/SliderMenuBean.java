package com.mp3.launcher4.beans;

/**
 * @author longzj
 */
public class SliderMenuBean {

    private int iconId;
    private int titleId;

    public SliderMenuBean(int iconId, int titleId) {
        this.titleId = titleId;
        this.iconId = iconId;
    }

    public SliderMenuBean() {
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }
}
