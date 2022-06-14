package com.giganet.giganet_worksheet.Presenter;

import com.giganet.giganet_worksheet.Model.ItemWorkStateModel;
import com.giganet.giganet_worksheet.Resources.Events.ItemRemovedEvent;
import com.giganet.giganet_worksheet.View.WorkStateFragments.ItemWorkStateContract;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class ItemWorkStatePresenter implements ItemWorkStateContract.Presenter {
    private final ItemWorkStateContract.Model model;
    private final ItemWorkStateContract.View view;

    public ItemWorkStatePresenter(ItemWorkStateContract.View view, int id, boolean must
            , int minNumberOfItems, String type, String username,String serviceType) {
        this.view = view;
        model = new ItemWorkStateModel(id, must, minNumberOfItems, type, username, serviceType);
        model.getItems(view.getActivity());
    }

    @Subscribe
    public void onItemRemovedEvent(ItemRemovedEvent event) {
        model.removeItems(view.getActivity(), event.getItem());
        view.setAdapter(model.getItems(view.getActivity()));
    }

    @Override
    public boolean isMust() {
        return model.isMust();
    }

    @Override
    public boolean isSet() {
        return model.isSet();
    }

    @Override
    public void addItem(String material, int quantity, String serialNum) {
        model.addItem(view.getActivity(), material, quantity, serialNum);
        view.setAdapter(model.getItems(view.getActivity()));
    }

    @Override
    public void modifyItem(String material, int quantity, String serialNum, int rowId) {
        model.modifyItem(view.getActivity(),material,quantity,serialNum,rowId);
        view.setAdapter(model.getItems(view.getActivity()));
    }

    @Override
    public void registForEvents() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void unregistForEvents() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void updateAdapter() {
        view.setAdapter(model.getItems(view.getActivity()));
    }

    @Override
    public String getTitle() {
        return model.getTitle();
    }

    @Override
    public String getServiceType() {
        return model.getServiceType();
    }


}
