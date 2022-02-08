package com.example.testing.db;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventsRepository {


    private final EventsDatabase eventsDatabase;

    public EventsRepository(Context context) {
        eventsDatabase = EventsDatabase.getInstance(context);
    }

    public void insertEvents(List<Events> events) {
        AsyncTask.execute(() -> eventsDatabase.eventsDao().insertAll(events.toArray(new Events[0])));
    }

    public LiveData<List<Events>> refreshEvents() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        MutableLiveData<List<Events>> events = new MutableLiveData<>();

        AsyncTask.execute(() -> {
            List<Events> oldEvents = eventsDatabase.eventsDao().getAllEvents();
            oldEvents.forEach(oldEvent -> {
                try {
                    Date triggerTime = dateFormat.parse(oldEvent.getTrigger_Time());
                    Date resetTime = dateFormat.parse(oldEvent.getReset_Time());

                    if (triggerTime != null) {
                        oldEvent.setTriggerTimeValue(triggerTime.getTime());
                    }
                    if (resetTime != null) {
                        oldEvent.setResetTimeValue(resetTime.getTime());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            eventsDatabase.eventsDao().insertAll(oldEvents.toArray(new Events[0]));

            events.postValue(oldEvents);
        });

        return events;
    }

    public LiveData<List<String>> getAllPrimaryLocations() {
        MutableLiveData<List<String>> primaryLocations = new MutableLiveData<>();

        AsyncTask.execute(() -> primaryLocations.postValue(eventsDatabase.eventsDao().getAllPrimaryLocations()));

        return primaryLocations;
    }

    public LiveData<List<String>> getAllSecondaryLocations() {
        MutableLiveData<List<String>> secondaryLocations = new MutableLiveData<>();

        AsyncTask.execute(() -> secondaryLocations.postValue(eventsDatabase.eventsDao().getAllSecondaryLocations()));

        return secondaryLocations;
    }

    public LiveData<List<String>> getAllEventTypes() {
        MutableLiveData<List<String>> eventTypes = new MutableLiveData<>();

        AsyncTask.execute(() -> eventTypes.postValue(eventsDatabase.eventsDao().getAllEventTypes()));

        return eventTypes;
    }

    public LiveData<List<Events>> filterEvents(List<String> primaryLocations, List<String> secondaryLocations, List<String> eventTypes, Long startDate) {
        MutableLiveData<List<Events>> events = new MutableLiveData<>();

        AsyncTask.execute(() -> events.postValue(eventsDatabase.eventsDao().filterEvents(
                primaryLocations.toArray(new String[0]),
                secondaryLocations.toArray(new String[0]),
                eventTypes.toArray(new String[0]),
                startDate,
                new Date().getTime()
        )));

        return events;
    }
}
