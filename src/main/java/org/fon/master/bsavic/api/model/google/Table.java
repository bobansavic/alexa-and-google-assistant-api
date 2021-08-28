package org.fon.master.bsavic.api.model.google;

import java.util.List;

public class Table {
    private String title, subtitle;
    private Image image;
    private List<TableColumn> columns;
    private List<TableRow> rows;
    private Link button;

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

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public List<TableColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<TableColumn> columns) {
        this.columns = columns;
    }

    public List<TableRow> getRows() {
        return rows;
    }

    public void setRows(List<TableRow> rows) {
        this.rows = rows;
    }

    public Link getButton() {
        return button;
    }

    public void setButton(Link button) {
        this.button = button;
    }
}
