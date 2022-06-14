package com.giganet.giganet_worksheet.Resources.Events;

public class NumberOfPictureChangeEvent {
    private final String workState;
    private final int numberOfPic;

    public NumberOfPictureChangeEvent(String workState, int numberOfPic) {
        this.workState = workState;
        this.numberOfPic = numberOfPic;
    }

    public String getWorkState() {
        return workState;
    }

    public int getNumberOfPic() {
        return numberOfPic;
    }
}
