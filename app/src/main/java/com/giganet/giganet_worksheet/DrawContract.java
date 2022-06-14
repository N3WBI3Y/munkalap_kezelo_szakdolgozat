package com.giganet.giganet_worksheet;

import android.app.Activity;
import android.content.Context;
import android.content.ServiceConnection;

import com.giganet.giganet_worksheet.View.PaintView;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public interface DrawContract {
    interface Model{
        ServiceConnection getConnection();

        CompletableFuture<Boolean> saveDrawing(Context context, PaintView paintView);

        File getFilePath();
    }

    interface Presenter{
        void bindService();

        void unbindService();

        void saveDrawing(PaintView paintView);

        File getFilePath();
    }

    interface View{
        Activity getActivity();

        void onFinish();
    }
}
