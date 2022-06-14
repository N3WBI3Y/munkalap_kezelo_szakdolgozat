package com.giganet.giganet_worksheet.Model;

import android.graphics.Path;

public class Draw {
    private final int color;
    private final int strokeWidth;
    private Path path;

    public Draw(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public int getColor() {
        return color;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public Path getPath() {
        return path;
    }
}
