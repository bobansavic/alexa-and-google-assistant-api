package org.fon.master.bsavic.api.model.google;

import java.util.List;

public class Collection {
    private String title, subtitle;
    private List<CollectionItem> items;
    private ImageFill imageFill;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public List<CollectionItem> getItems() {
        return items;
    }

    public void setItems(List<CollectionItem> items) {
        this.items = items;
    }

    public ImageFill getImageFill() {
        return imageFill;
    }

    public void setImageFill(ImageFill imageFill) {
        this.imageFill = imageFill;
    }
}
