package org.fon.master.bsavic.api.model.google;

import java.util.List;

public class TableRow {
    private List<TableCell> cells;
    private boolean divider;

    public List<TableCell> getCells() {
        return cells;
    }

    public void setCells(List<TableCell> cells) {
        this.cells = cells;
    }

    public boolean isDivider() {
        return divider;
    }

    public void setDivider(boolean divider) {
        this.divider = divider;
    }
}
