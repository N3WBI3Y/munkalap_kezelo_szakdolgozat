package com.giganet.giganet_worksheet.Model;

public class WorkStateModel {
    protected final boolean must;
    protected final int workId;
    protected final String title;
    protected final String username;

    public WorkStateModel(boolean must, int workId, String title, String username) {
        this.must = must;
        this.workId = workId;
        this.title = title;
        this.username = username;
    }

    public boolean isMust() {
        return must;
    }

    public int getWorkId() {
        return workId;
    }

    public String getTitle() {
        return title;
    }

    public String getUsername() {
        return username;
    }
}
