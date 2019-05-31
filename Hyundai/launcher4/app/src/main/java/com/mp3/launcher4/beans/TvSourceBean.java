package com.mp3.launcher4.beans;

/**
 * @author longzj
 */
public class TvSourceBean implements Cloneable {
    public final static int SOURCE_USB = -1;
    private int tvNameId;
    private int inputsource;
    private int sourceIcon;

    public TvSourceBean(int tvNameId, int inputsource, int sourceIcon) {
        this.tvNameId = tvNameId;
        this.inputsource = inputsource;
        this.sourceIcon = sourceIcon;
    }

    public int getTvNameId() {
        return tvNameId;
    }

    public void setTvNameId(int tvNameId) {
        this.tvNameId = tvNameId;
    }

    public int getInputsource() {
        return inputsource;
    }

    public void setInputsource(int inputsource) {
        this.inputsource = inputsource;
    }

    public int getSourceIcon() {
        return sourceIcon;
    }

    public void setSourceIcon(int sourceIcon) {
        this.sourceIcon = sourceIcon;
    }

    @Override
    public TvSourceBean clone() {
        TvSourceBean bean = null;
        try {
            bean = (TvSourceBean) super.clone();
        } catch (Exception ignore) {

        }
        return bean;
    }
}
