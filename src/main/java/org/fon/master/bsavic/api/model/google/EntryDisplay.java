package org.fon.master.bsavic.api.model.google;

public class EntryDisplay {
    private String title;
    private String description;
    private Image image;
    private String footer;
    private OpenUrl openUrl;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public OpenUrl getOpenUrl() {
        return openUrl;
    }

    public void setOpenUrl(OpenUrl openUrl) {
        this.openUrl = openUrl;
    }
}
