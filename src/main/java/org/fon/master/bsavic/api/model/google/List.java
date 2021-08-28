package org.fon.master.bsavic.api.model.google;

public class List {
    private String title, subtitle;
    private java.util.List<ListItem> items;

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

    public java.util.List<ListItem> getItems() {
        return items;
    }

    public void setItems(java.util.List<ListItem> items) {
        this.items = items;
    }
}
