package com.giganet.giganet_worksheet.Model;

import android.content.Context;

import com.giganet.giganet_worksheet.Persistence.Worksheet.Entities.InstallationTransactionEntity;
import com.giganet.giganet_worksheet.Persistence.Worksheet.InstallationTransactionTableHandler;
import com.giganet.giganet_worksheet.Resources.Enums.DBStatus;
import com.giganet.giganet_worksheet.Utils.DateHelper;
import com.giganet.giganet_worksheet.View.WorkStateFragments.ItemWorkStateContract;

import java.util.ArrayList;

public class ItemWorkStateModel extends WorkStateModel implements ItemWorkStateContract.Model {
    private final int minNumberOfItems;
    private ArrayList<InstallationTransactionEntity> items;
    private final String serviceType;

    public ItemWorkStateModel(int id, boolean must, int minNumberOfItems
                            , String type, String username, String serviceType) {
        super(must,id,type,username);
        this.minNumberOfItems = minNumberOfItems;
        items = new ArrayList<>();
        this.serviceType = serviceType;
    }

    public void removeItems(Context context, int itemIndex) {
        InstallationTransactionTableHandler installationTransactionTableHandler = new InstallationTransactionTableHandler(context);
        installationTransactionTableHandler.removeItem(itemIndex);
    }

    @Override
    public String getServiceType() {
        return serviceType;
    }


    @Override
    public boolean isMust() {
        return must;
    }

    @Override
    public boolean isSet() {
        return items.size() >= minNumberOfItems;
    }

    @Override
    public void addItem(Context context, String material, int quantity, String serialNum) {
        InstallationTransactionTableHandler transactionTableHandler = new InstallationTransactionTableHandler(context);
        transactionTableHandler.addTransaction(workId,username, DateHelper.getCurrentDate(),material,quantity,serialNum, DBStatus.CREATED.value);
    }

    @Override
    public void modifyItem(Context context, String material, int quantity, String serialNum, int rowId) {
        InstallationTransactionTableHandler transactionTableHandler = new InstallationTransactionTableHandler(context);
        transactionTableHandler.updateTransaction(rowId,DateHelper.getCurrentDate(),material,quantity,serialNum,DBStatus.CREATED.value);
    }

    @Override
    public ArrayList<InstallationTransactionEntity> getItems(Context context) {
        InstallationTransactionTableHandler installationTransactionTableHandler = new InstallationTransactionTableHandler(context);
        items = installationTransactionTableHandler.getWorkTransaction(workId,username);
        return items;
    }
}
