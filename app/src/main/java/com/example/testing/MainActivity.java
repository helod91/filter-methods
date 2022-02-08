package com.example.testing;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testing.db.Events;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final int EVERYTHING = 0;
    public static final int LAST_30_HOURS = 1;
    public static final int LAST_24_HOURS = 2;
    public static final int LAST_12_HOURS = 3;
    public static final int LAST_7_HOURS = 4;

    public static final int TIME_ASC = 1;
    public static final int TIME_DESC = 2;
    public static final int DURATION_ASC = 3;
    public static final int DURATION_DESC = 4;

    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
    private final EventAdapter adapter = new EventAdapter();
    protected MainViewModel viewModel;
    private List<String> chosenPrimaryLocations;
    private List<String> chosenSecondaryLocations;
    private List<String> chosenEventType;
    private Long chosenStartDate;
    private int chosenSorting = TIME_ASC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.initRepository(this);

        createDummyEvents();
        setTimeFrame();
        setSortBy();

        RecyclerView events = findViewById(R.id.main_events);
        events.setLayoutManager(new LinearLayoutManager(this));
        events.setAdapter(adapter);

        viewModel.refreshEvents().observe(this, adapter::setData);
        viewModel.getPrimaryLocations().observe(this, locations -> {
            setPrimaryLocations(locations);

            filterEvents();
        });
        viewModel.getSecondaryLocations().observe(this, locations -> {
            setSecondaryLocations(locations);

            filterEvents();
        });
        viewModel.getEventTypes().observe(this, types -> {
            setEventTypes(types);

            filterEvents();
        });
    }

    private void setPrimaryLocations(List<String> locations) {
        chosenPrimaryLocations = locations;

        MultiSpinner spinner = findViewById(R.id.main_primary_locations);
        spinner.setItems(locations, "All Primary Locations", selected -> {
            chosenPrimaryLocations = new ArrayList<>();
            for (int i = 0; i < locations.size(); i++) {
                if (selected[i]) {
                    chosenPrimaryLocations.add(locations.get(i));
                }
            }

            filterEvents();
        });
    }

    private void setSecondaryLocations(List<String> locations) {
        chosenSecondaryLocations = locations;

        MultiSpinner spinner = findViewById(R.id.main_secondary_locations);
        spinner.setItems(locations, "All Secondary Locations", selected -> {
            chosenSecondaryLocations = new ArrayList<>();
            for (int i = 0; i < locations.size(); i++) {
                if (selected[i]) {
                    chosenSecondaryLocations.add(locations.get(i));
                }
            }

            filterEvents();
        });
    }

    private void setEventTypes(List<String> types) {
        chosenEventType = types;

        String[] typeArray = new String[types.size() + 1];
        typeArray[0] = "All Event Types";
        for (int i = 0; i < types.size(); i++) {
            typeArray[i + 1] = types.get(i);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, typeArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        AppCompatSpinner spinner = findViewById(R.id.main_event_types);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    chosenEventType = types;
                } else {
                    chosenEventType = new ArrayList<>();
                    chosenEventType.add(types.get(position - 1));
                }

                filterEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setTimeFrame() {
        Calendar initialStartDate = Calendar.getInstance();
        initialStartDate.setTime(new Date());
        initialStartDate.set(Calendar.YEAR, 1970);

        chosenStartDate = initialStartDate.getTimeInMillis();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item, getResources().getStringArray(R.array.time_frame));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        AppCompatSpinner spinner = findViewById(R.id.main_time_frame);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                Calendar startDate = Calendar.getInstance();
                startDate.setTime(new Date());
                switch (position) {
                    case EVERYTHING:
                        startDate.set(Calendar.YEAR, 1970);
                        break;
                    case LAST_30_HOURS:
                        startDate.setTimeInMillis(
                                startDate.getTimeInMillis() - (30 * 60 * 60 * 1000)
                        );
                        break;
                    case LAST_24_HOURS:
                        startDate.setTimeInMillis(
                                startDate.getTimeInMillis() - (24 * 60 * 60 * 1000)
                        );
                        break;
                    case LAST_12_HOURS:
                        startDate.setTimeInMillis(
                                startDate.getTimeInMillis() - (12 * 60 * 60 * 1000)
                        );
                        break;
                    case LAST_7_HOURS:
                        startDate.setTimeInMillis(
                                startDate.getTimeInMillis() - (7 * 60 * 60 * 1000)
                        );
                }

                chosenStartDate = startDate.getTimeInMillis();
                filterEvents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setSortBy() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, getResources().getStringArray(R.array.sort_by)) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                if (position == 0) {
                    view.setTextColor(Color.GRAY);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        AppCompatSpinner spinner = findViewById(R.id.main_sort_by);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                if (position == 0) {
                    ((TextView) view).setTextColor(Color.GRAY);
                } else {
                    chosenSorting = position;
                    filterEvents();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void filterEvents() {
        if (chosenPrimaryLocations != null && chosenSecondaryLocations != null && chosenEventType != null) {
            viewModel.filterEvents(chosenPrimaryLocations, chosenSecondaryLocations, chosenEventType, chosenStartDate).observe(this, events -> {
                sortList(events);
                adapter.setData(events);
            });
        }
    }

    private void sortList(List<Events> events) {
        switch (chosenSorting) {
            case TIME_ASC:
                events.sort(Comparator.comparing(Events::getTrigger_Time));
                break;
            case TIME_DESC:
                events.sort((lsh, rsh) -> rsh.getTrigger_Time().compareTo(lsh.getTrigger_Time()));
                break;
            case DURATION_ASC:
                events.sort((lsh, rsh) -> {
                    long firstDifference = lsh.getResetTimeValue() - lsh.getTriggerTimeValue();
                    long secondDifference = rsh.getResetTimeValue() - rsh.getTriggerTimeValue();

                    return Long.compare(firstDifference, secondDifference);
                });
                break;
            case DURATION_DESC:
                events.sort((lsh, rsh) -> {
                    long firstDifference = lsh.getResetTimeValue() - lsh.getTriggerTimeValue();
                    long secondDifference = rsh.getResetTimeValue() - rsh.getTriggerTimeValue();

                    return Long.compare(secondDifference, firstDifference);
                });
        }
    }

    private void createDummyEvents() {
        String[] eventTypes = new String[]{"Call", "Device Lost", "Discovered", "Alarm"};
        ArrayList<Events> dummyEvents = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            String primary = "Room " + new Random().nextInt(10);
            String secondary = "Bed " + new Random().nextInt(10);
            String type = eventTypes[new Random().nextInt(4)];
            Calendar time = Calendar.getInstance();
            time.setTime(new Date());

            long minusTime = new Random().nextInt(48) * 60 * 60 * 1000 +
                    new Random().nextInt(60) * 60 * 1000 +
                    new Random().nextInt(60) * 1000;
            time.setTimeInMillis(time.getTimeInMillis() - minusTime);

            String triggerTime = dateFormat.format(time.getTime());
            long triggerTimeValue = time.getTimeInMillis();

            if (new Random().nextInt(2) % 2 == 0) {
                time.setTimeInMillis(time.getTimeInMillis() + new Random().nextInt(120000));
            }

            String resetTime = dateFormat.format(time.getTime());
            long resetTimeValue = time.getTimeInMillis();

            Events dummy = new Events(
                    primary,
                    secondary,
                    type,
                    triggerTime,
                    resetTime,
                    "0",
                    triggerTimeValue,
                    resetTimeValue
            );
            dummy.setId(i);

            dummyEvents.add(dummy);
        }

        viewModel.insertDummyEvents(dummyEvents);
    }
}