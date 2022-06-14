package com.giganet.giganet_worksheet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public interface AuthenticationContract {

    interface Model {
        boolean isValidUsername();

        boolean isValidPassword();

        String getWelcomeMessage();

        void syncTypesWithBackEnds(Context activity);

        void savePreference(SharedPreferences.Editor editor, String username, boolean rememberMe);

        void saveEncryptedUser(SharedPreferences.Editor editor);
    }

    interface Presenter {
        void validate(String username, String password, Context context);

        void doLogin(String username, String password);

        void savePreference(SharedPreferences.Editor editor, String username, boolean rememberMe);
    }

    interface View {
        void onSuccess(String msg);

        void onError(String msg);

        void login();

        Activity getActivity();

    }
}
