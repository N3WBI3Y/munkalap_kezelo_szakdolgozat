package com.giganet.giganet_worksheet.Resources.Events;

public class ItemRemovedEvent {
    private final int item;

    public ItemRemovedEvent(int item) {
        this.item = item;
    }

    public int getItem() {
        return item;
    }
}
