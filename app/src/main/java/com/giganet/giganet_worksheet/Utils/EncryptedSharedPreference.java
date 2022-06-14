package com.giganet.giganet_worksheet.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class EncryptedSharedPreference {
    private SharedPreferences encryptedSharedPreference;

    public EncryptedSharedPreference(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);

            encryptedSharedPreference = EncryptedSharedPreferences.create(
                    com.giganet.giganet_worksheet.Resources.Constants.SharedPreferences.ENCRYPTEDSHAREDPREFENCES,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public SharedPreferences getEncryptedSharedPreference() {
        return encryptedSharedPreference;
    }
}
