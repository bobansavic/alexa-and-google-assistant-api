package org.fon.master.bsavic.api.model.google;

public class TableColumn {
    private String header;
    private HorizontalAlignment horizontalAlignment;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }
}
