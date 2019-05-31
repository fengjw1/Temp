package com.ktc.ecuador.data.protocal;

/**
 * @author longzj
 */
public class TvSourceBean implements Cloneable {
    public final static int SOURCE_USB = -1;
    private int tvNameId;
    private int inputsource;

    public TvSourceBean(int tvNameId, int inputsource) {
        this.tvNameId = tvNameId;
        this.inputsource = inputsource;
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
