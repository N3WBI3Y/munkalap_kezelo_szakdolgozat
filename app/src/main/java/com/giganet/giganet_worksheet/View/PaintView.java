package com.giganet.giganet_worksheet.View;

import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.giganet.giganet_worksheet.Model.Draw;
import com.giganet.giganet_worksheet.Resources.Constants.ServiceConstants;

import java.io.File;
import java.util.ArrayList;

public class PaintView extends View {

    private float x, y;
    private Path path;
    private final Paint paint;
    private final int color = ServiceConstants.Draw.BRUSH_COLOR;
    private Bitmap bitmap;
    private Canvas canvas;
    private final Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);

    private final ArrayList<Draw> paths = new ArrayList<>();

    public PaintView(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(ServiceConstants.Draw.BRUSH_SIZE);
        paint.setXfermode(null);
        paint.setAlpha(0xff);
    }

    public void init(DisplayMetrics metrics){
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        int bgColor = ServiceConstants.Draw.BG_COLOR;
        this.canvas.drawColor(bgColor);

        for (Draw draw : paths) {
            paint.setMaskFilter(null);
            this.canvas.drawPath(draw.getPath(), paint);
        }

        canvas.drawBitmap(bitmap, 0,0,bitmapPaint);
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x,y);
                invalidate();
                break;
        }
        return true;
    }

    private void touchStart(float x, float y){
        path = new Path();
        int strokeWidth = ServiceConstants.Draw.BRUSH_SIZE;
        Draw draw = new Draw(color, strokeWidth,path);
        paths.add(draw);

        path.reset();
        path.moveTo(x,y);

        this.x = x;
        this.y = y;
    }

    private void touchMove(float x, float y){
        float dx = Math.abs(x - this.x);
        float dy = Math.abs(y - this.y);

        if (dx >= ServiceConstants.Draw.TOUCH_TOLERANCE || dy >= ServiceConstants.Draw.TOUCH_TOLERANCE){
            path.quadTo(this.x, this.y, (x + this.x) / 2, (y + this.y) / 2);
            this.x = x;
            this.y = y;
        }
    }

    private void touchUp(){
        path.lineTo(x, y);
    }

    public void clearCanvas(){
        paths.clear();
        invalidate();
    }

    public Bitmap getDrawBitmap(){
        return bitmap;
    }


}
