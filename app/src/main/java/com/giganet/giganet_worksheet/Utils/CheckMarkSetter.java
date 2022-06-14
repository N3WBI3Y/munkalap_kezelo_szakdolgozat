package com.giganet.giganet_worksheet.Utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import com.example.giganet_worksheet.R;

public class CheckMarkSetter {
    public static void setCheckMark(ImageView markView, boolean type, boolean must, Context context) {
        if (must) {
            setCheckMarkMust(markView, type, context);
        } else {
            setCheckMark(markView, type, context);
        }
    }

    private static void setCheckMarkMust(ImageView markView, boolean type, Context context) {
        Drawable normalImage = type ? ResourcesCompat.getDrawable(context.getResources(), R.drawable.green_check_mark, null)
                : ResourcesCompat.getDrawable(context.getResources(), R.drawable.exclamation_mark_icon, null);
        markView.setImageDrawable(normalImage);

    }

    private static void setCheckMark(ImageView markView, boolean type, Context context) {
        Drawable normalImage = type ? ResourcesCompat.getDrawable(context.getResources(), R.drawable.green_check_mark, null)
                : ResourcesCompat.getDrawable(context.getResources(), R.drawable.question_mark, null);
        markView.setImageDrawable(normalImage);

    }

}
