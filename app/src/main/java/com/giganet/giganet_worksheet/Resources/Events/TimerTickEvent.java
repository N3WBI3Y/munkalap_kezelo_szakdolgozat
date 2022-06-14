package com.giganet.giganet_worksheet.Resources.Events;

public class TimerTickEvent {
    private final long elapsed_time;
    private final int workId;

    public TimerTickEvent(long elapsed_time, int workId) {
        this.elapsed_time = elapsed_time;
        this.workId = workId;
    }

    public long getElapsed_time() {
        return elapsed_time;
    }

    public int getWorkId() {
        return workId;
    }
}
