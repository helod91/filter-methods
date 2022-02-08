package com.example.testing.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface EventsDao {

    @Query("SELECT Primary_Location FROM events GROUP BY Primary_Location")
    List<String> getAllPrimaryLocations();

    @Query("SELECT Secondary_Location FROM events GROUP BY Secondary_Location")
    List<String> getAllSecondaryLocations();

    @Query("SELECT Event_Type FROM events GROUP BY Event_Type")
    List<String> getAllEventTypes();

    @Query("SELECT * " +
            "FROM events " +
            "WHERE Primary_Location IN (:primaryLocations) " +
            "AND Secondary_Location IN (:secondaryLocations) " +
            "AND Event_Type IN (:eventTypes)" +
            "AND TriggerTimeValue BETWEEN :startDate AND :endDate")
    List<Events> filterEvents(
            String[] primaryLocations,
            String[] secondaryLocations,
            String[] eventTypes,
            Long startDate,
            Long endDate
    );

    @Query("SELECT * FROM Events")
    List<Events> getAllEvents();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Events... events);
}
