package com.example.testing;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.testing.db.Events;
import com.example.testing.db.EventsRepository;

import java.util.List;

public class MainViewModel extends ViewModel {

    private EventsRepository repository;

    public void initRepository(Context context) {
        repository = new EventsRepository(context);
    }

    public void insertDummyEvents(List<Events> events) {
        repository.insertEvents(events);
    }

    public LiveData<List<Events>> refreshEvents() {
        return repository.refreshEvents();
    }

    public LiveData<List<String>> getPrimaryLocations() {
        return repository.getAllPrimaryLocations();
    }

    public LiveData<List<String>> getSecondaryLocations() {
        return repository.getAllSecondaryLocations();
    }

    public LiveData<List<String>> getEventTypes() {
        return repository.getAllEventTypes();
    }

    public LiveData<List<Events>> filterEvents(List<String> primaryLocations, List<String> secondaryLocations, List<String> eventTypes, Long startDate) {
        return repository.filterEvents(primaryLocations, secondaryLocations, eventTypes, startDate);
    }
}
