package com.giganet.giganet_worksheet.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
    SharedPreferences sharedPreferences = null;

    public SharedPreference(Context context) {
        this.sharedPreferences = context.getApplicationContext().getSharedPreferences(com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences.SHAREDPREFERENCES, Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}
