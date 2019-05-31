package com.mp3.launcher4.networks.responses;

/**
 * @author longzj
 */
public class BannerResponse {

    /**
     * title : PlayKids
     * image : http://8810213628.staging.foxxum.com/files/images/banner/banner_625x300.jpg
     * url : http://8810213628.staging.foxxum.com/open.php?type=app&id=1366
     */

    private String title;
    private String image;
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
