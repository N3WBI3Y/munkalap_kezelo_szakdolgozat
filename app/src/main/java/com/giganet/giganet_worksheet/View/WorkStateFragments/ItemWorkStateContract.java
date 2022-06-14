package com.giganet.giganet_worksheet.View.WorkStateFragments;

import android.app.Activity;
import android.content.Context;

import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTransactionEntity;

import java.util.ArrayList;

public interface ItemWorkStateContract {
    interface Model {
        boolean isMust();

        boolean isSet();

        void addItem(Context context, String material, int quantity, String serialNum);

        void modifyItem(Context context,String material, int quantity, String serialNum, int rowId);

        ArrayList<InstallationTransactionEntity> getItems(Context context);

        void removeItems(Context context, int itemIndex);

        String getTitle();

        String getServiceType();
    }

    interface Presenter {
        boolean isMust();

        boolean isSet();

        void addItem(String material, int quantity, String serialNum);

        void modifyItem(String material, int quantity, String serialNum, int rowId);

        void registForEvents();

        void unregistForEvents();

        void updateAdapter();

        String getTitle();

        String getServiceType();
    }

    interface View {
        void setAdapter(ArrayList<InstallationTransactionEntity> items);

        Activity getActivity();

    }
}
