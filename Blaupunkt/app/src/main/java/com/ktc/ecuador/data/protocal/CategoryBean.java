package com.ktc.ecuador.data.protocal;

import java.util.List;
import java.util.Objects;

public class CategoryBean {

    /**
     * categories : [{"id":1,"label":"MOVIES","display":"Movies","image":"http://api.app.fxmconnect.com/_misc/category_image/1.jpg"},{"id":2,"label":"TVSHOWS","display":"TV Shows","image":"http://api.app.fxmconnect.com/_misc/category_image/2.jpg"},{"id":3,"label":"KIDS","display":"Kids","image":"http://api.app.fxmconnect.com/_misc/category_image/3.jpg"},{"id":5,"label":"MUSIC","display":"Music","image":"http://api.app.fxmconnect.com/_misc/category_image/5.jpg"},{"id":6,"label":"NEWS","display":"News","image":"http://api.app.fxmconnect.com/_misc/category_image/6.jpg"},{"id":7,"label":"SPORT","display":"Sport","image":"http://api.app.fxmconnect.com/_misc/category_image/7.jpg"},{"id":99,"label":"RECOMMENDED","display":"Recommended","image":"http://api.app.fxmconnect.com/_misc/category_image/99.jpg"}]
     * hash : 0c1f765e27a0e842c1c4ad13201ec2b2
     */

    private String hash;
    private List<CategoryItem> categories;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<CategoryItem> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryItem> categories) {
        this.categories = categories;
    }

    public static class CategoryItem {
        /**
         * id : 1
         * label : MOVIES
         * display : Movies
         * image : http://api.app.fxmconnect.com/_misc/category_image/1.jpg
         */

        private int id;
        private String label;
        private String display;
        private String image;

        @Override
        public int hashCode() {

            return Objects.hash(id, label, display, image);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CategoryItem that = (CategoryItem) o;
            return id == that.id &&
                    Objects.equals(label, that.label) &&
                    Objects.equals(display, that.display) &&
                    Objects.equals(image, that.image);
        }

        @Override
        public String toString() {
            return "CategoryItem{" +
                    "id=" + id +
                    ", label='" + label + '\'' +
                    ", display='" + display + '\'' +
                    ", image='" + image + '\'' +
                    '}';
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
    }

    @Override
    public String toString() {
        return "CategoryBean{" +
                "hash='" + hash + '\'' +
                ", categories=" + categories +
                '}';
    }
}
