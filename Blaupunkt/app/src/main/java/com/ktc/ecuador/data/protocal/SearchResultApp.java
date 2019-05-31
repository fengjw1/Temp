package com.ktc.ecuador.data.protocal;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SearchResultApp {

    /**
     * list : [{"back_keycode":"-1","icon-square":"https://file2fxm.azureedge.net/files/images/app_icons/Images_240x240_angled/meteox.jpg","id":"150","image":"https://file2fxm.azureedge.net/files/images/app_icons/Images_384x216_angled/meteox.jpg","title":"MeteoX","url":"http://3183393180-7017024353.am-live-2.fxmconnect.com/open.php?type=app&id=150"},{"back_keycode":"-1","icon-square":"https://file2fxm.azureedge.net/files/images/app_icons/Images_240x240_angled/prextremetv.jpg","id":"1044","image":"https://file2fxm.azureedge.net/files/images/app_icons/Images_384x216_angled/prextremetv.jpg","title":"PRExtremeTV","url":"http://3183393180-7017024353.am-live-2.fxmconnect.com/open.php?type=app&id=1044"}]
     * expireDate : 1550748939
     */

    private int expireDate;
    private List<ListBean> list;

    public int getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(int expireDate) {
        this.expireDate = expireDate;
    }

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean extends ItemBean {
        /**
         * back_keycode : -1
         * icon-square : https://file2fxm.azureedge.net/files/images/app_icons/Images_240x240_angled/meteox.jpg
         * id : 150
         * image : https://file2fxm.azureedge.net/files/images/app_icons/Images_384x216_angled/meteox.jpg
         * title : MeteoX
         * url : http://3183393180-7017024353.am-live-2.fxmconnect.com/open.php?type=app&id=150
         */

        @SerializedName("icon-square")
        private String iconsquare;


        public String getIconsquare() {
            return iconsquare;
        }

        public void setIconsquare(String iconsquare) {
            this.iconsquare = iconsquare;
        }


    }
}
