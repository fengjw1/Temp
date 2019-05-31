package com.ktc.ecuador.data.protocal;

import java.util.List;
import java.util.Objects;

public class StoreApp {

    /**
     * categoryId : 1
     * list : [{"id":"46","image":"http://8810213628.staging.foxxum.com/files/images/app_icons/Images_300x196_angled/dailymotion.png","title":"Dailymotion","url":"http://8810213628.staging.foxxum.com/open.php?type=app&id=46"},{"id":"6","image":"http://8810213628.staging.foxxum.com/files/images/app_icons/Images_300x196_angled/pocoyo.png","title":"Pocoyo","url":"http://8810213628.staging.foxxum.com/open.php?type=app&id=6"},{"id":"7","image":"http://8810213628.staging.foxxum.com/files/images/app_icons/Images_300x196_angled/canalcocina.png","title":"Canal Cocina","url":"http://8810213628.staging.foxxum.com/open.php?type=app&id=7"},{"id":"685","image":"http://8810213628.staging.foxxum.com/files/images/app_icons/Images_300x196_angled/deutschewelle.png","title":"Deutsche Welle","url":"http://8810213628.staging.foxxum.com/open.php?type=app&id=685"},{"id":"400","image":"http://8810213628.staging.foxxum.com/files/images/app_icons/Images_300x196_angled/blancoynegro.png","title":"Blanco y Negro","url":"http://8810213628.staging.foxxum.com/open.php?type=app&id=400"},{"id":"239","image":"http://8810213628.staging.foxxum.com/files/images/app_icons/Images_300x196_angled/historia.png","title":"Canal Historia","url":"http://8810213628.staging.foxxum.com/open.php?type=app&id=239"},{"id":"296","image":"http://8810213628.staging.foxxum.com/files/images/app_icons/Images_300x196_angled/hellokids.png","title":"Hello Kids","url":"http://8810213628.staging.foxxum.com/open.php?type=app&id=296"},{"id":"230","image":"http://8810213628.staging.foxxum.com/files/images/app_icons/Images_300x196_angled/howdini.png","title":"Howdini","url":"http://8810213628.staging.foxxum.com/open.php?type=app&id=230"},{"id":"95","image":"http://8810213628.staging.foxxum.com/files/images/app_icons/Images_300x196_angled/nrj.png","title":"NRJ","url":"http://8810213628.staging.foxxum.com/open.php?type=app&id=95"},{"id":"750","image":"http://8810213628.staging.foxxum.com/files/images/app_icons/Images_300x196_angled/gaylestv.png","title":"GAYLES TV","url":"http://8810213628.staging.foxxum.com/open.php?type=app&id=750"}]
     * hash : 72d0b84f7327c08961e40f256fd791b0
     * expireDate : 1529801160
     */

    private int categoryId;
    private String hash;
    private int expireDate;
    private List<ListBean> list;

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

    public List<ListBean> getList() {
        return list;
    }

    public void setList(List<ListBean> list) {
        this.list = list;
    }

    public static class ListBean {
        /**
         * id : 46
         * image : http://8810213628.staging.foxxum.com/files/images/app_icons/Images_300x196_angled/dailymotion.png
         * title : Dailymotion
         * url : http://8810213628.staging.foxxum.com/open.php?type=app&id=46
         */
        private int back_keycode = -1;
        private String id = "noId";
        private String image;
        private String title;
        private String url;

        @Override
        public int hashCode() {

            return Objects.hash(back_keycode, id, image, title, url);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ListBean listBean = (ListBean) o;
            return back_keycode == listBean.back_keycode &&
                    Objects.equals(id, listBean.id) &&
                    Objects.equals(image, listBean.image) &&
                    Objects.equals(title, listBean.title) &&
                    Objects.equals(url, listBean.url);
        }

        @Override
        public String toString() {
            return "ListBean{" +
                    "back_keycode=" + back_keycode +
                    ", id='" + id + '\'' +
                    ", image='" + image + '\'' +
                    ", title='" + title + '\'' +
                    ", url='" + url + '\'' +
                    '}';
        }

        public int getBack_keycode() {
            return back_keycode;
        }

        public void setBack_keycode(int back_keycode) {
            this.back_keycode = back_keycode;
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
    }
}
