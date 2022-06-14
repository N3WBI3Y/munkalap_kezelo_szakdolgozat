package com.giganet.giganet_worksheet.Presenter;

import com.giganet.giganet_worksheet.Model.MapWorkStateModel;
import com.giganet.giganet_worksheet.Resources.Events.LocationEvent;
import com.giganet.giganet_worksheet.View.WorkStateFragments.MapWorkStateContract;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class MapWokStatePresenter implements MapWorkStateContract.Presenter {
    private final MapWorkStateContract.Model model;
    private final MapWorkStateContract.View view;

    public MapWokStatePresenter(MapWorkStateContract.View view, int workId, boolean must, String type, String username) {
        this.model = new MapWorkStateModel(workId, must, type, username);
        this.view = view;
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
    public void setCoordinates(double lon, double lat) {
        model.setCoordinates(view.getActivity(), lon, lat);
        view.setCoordinates(model.getLon(), model.getLat());
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
    public String getTitle() {
        return model.getTitle();
    }

    @Override
    public String getUsername() {
        return model.getUsername();
    }

    @Subscribe
    public void onNewLocationEvent(LocationEvent event) {
        setCoordinates(event.getLocation().getLongitude(), event.getLocation().getLatitude());
    }
}
