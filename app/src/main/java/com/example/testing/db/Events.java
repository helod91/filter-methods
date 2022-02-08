package com.example.testing.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Events {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String Primary_Location;
    private String Secondary_Location;
    private String Event_Type;
    private String Trigger_Time;
    private String Reset_Time;
    private String Device_Id;
    private Long TriggerTimeValue;
    private Long ResetTimeValue;

    public Events() {

    }

    public Events(@NonNull String primary_Location, String secondary_Location, String event_Type, String trigger_Time, String reset_Time, String device_Id, Long triggerTimeValue, Long resetTimeValue) {
        Primary_Location = primary_Location;
        Secondary_Location = secondary_Location;
        Event_Type = event_Type;
        Trigger_Time = trigger_Time;
        Reset_Time = reset_Time;
        Device_Id = device_Id;
        TriggerTimeValue = triggerTimeValue;
        ResetTimeValue = resetTimeValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getPrimary_Location() {
        return Primary_Location;
    }

    public void setPrimary_Location(@NonNull String primary_Location) {
        Primary_Location = primary_Location;
    }

    public String getSecondary_Location() {
        return Secondary_Location;
    }

    public void setSecondary_Location(String secondary_Location) {
        Secondary_Location = secondary_Location;
    }

    public String getEvent_Type() {
        return Event_Type;
    }

    public void setEvent_Type(String event_Type) {
        Event_Type = event_Type;
    }

    public String getTrigger_Time() {
        return Trigger_Time;
    }

    public void setTrigger_Time(String trigger_Time) {
        Trigger_Time = trigger_Time;
    }

    public String getReset_Time() {
        return Reset_Time;
    }

    public void setReset_Time(String reset_Time) {
        Reset_Time = reset_Time;
    }

    public String getDevice_Id() {
        return Device_Id;
    }

    public void setDevice_Id(String device_Id) {
        Device_Id = device_Id;
    }

    public Long getTriggerTimeValue() {
        return TriggerTimeValue;
    }

    public void setTriggerTimeValue(Long triggerTimeValue) {
        TriggerTimeValue = triggerTimeValue;
    }

    public Long getResetTimeValue() {
        return ResetTimeValue;
    }

    public void setResetTimeValue(Long resetTimeValue) {
        ResetTimeValue = resetTimeValue;
    }
}
