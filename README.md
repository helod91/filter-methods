# How to implement the filter methods inside your app

#### New classes and resources
- strings.xml contains the following string arrays used in the dropdowns:
```xml
<string-array name="time_frame">
        <item>Everything</item>
        <item>Last 30 hours</item>
        <item>Last 24 hours</item>
        <item>Last 12 hours</item>
        <item>Last 7 hours</item>
    </string-array>

    <string-array name="sort_by">
        <item>Sort by</item>
        <item>Time asc.</item>
        <item>Time desc.</item>
        <item>Duration asc.</item>
        <item>Duration desc.</item>
    </string-array>
```
- Inside the layouts you'll find `spinner_item.xml` for the dropdowns.
- `MultiSpinner.java` is a new widget class that is used for primary and secondary locations (when we want to select more then one item from the dropdown)

#### Small changes
- One small, but significant change can be found in the database class, where I incremented the version from 2 to 3 with a migration: I had to add the trigger time and reset time as milliseconds so I can properly filter them.
 - Increment the version number by one (if it's currently 1, then put 2):
```java
@Database(entities = {Events.class}, version = 3)
```
 - Add the migration that will add two new columns into the Events entity: 
 ```java
static final Migration MIGRATION_2_3 = new Migration(2, 3) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Events ADD COLUMN TriggerTimeValue INTEGER");
            database.execSQL("ALTER TABLE Events ADD COLUMN ResetTimeValue INTEGER");
        }
    };
```
 - Use this migration when creating the DB instance: 
 ```java
Room.databaseBuilder(context, EventsDatabase.class, "events-db")
                        .addMigrations(MIGRATION_2_3)
                        .build();
```
 - If all of these where added, we should have a class that looks like:
 ```java
@Database(entities = {Events.class}, version = 3)
public abstract class EventsDatabase extends RoomDatabase {

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Events ADD COLUMN TriggerTimeValue INTEGER");
            database.execSQL("ALTER TABLE Events ADD COLUMN ResetTimeValue INTEGER");
        }
    };
    private static volatile EventsDatabase INSTANCE;

    public static EventsDatabase getInstance(Context context) {
        synchronized (EventsDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, EventsDatabase.class, "events-db")
                        .addMigrations(MIGRATION_2_3)
                        .build();
            }
        }

        return INSTANCE;
    }

    abstract EventsDao eventsDao();
}
```
- As mentioned above, I've added two new columns to the Events entitiy, which looks like this in the java class:
```java
@Entity
public class Events {

    @PrimaryKey(autoGenerate = true)
    private int id;
 
	...other fields
    
    private Long TriggerTimeValue;
    private Long ResetTimeValue;
	
	... constructors, getters/setters
}
```

#### New methods

- Inside the EventsDao you'll find a couple new methods:
 - The following returns all the primary locations stored:
  ```java
    @Query("SELECT Primary_Location FROM events GROUP BY Primary_Location")
    List<String> getAllPrimaryLocations();
```
 - The following returns all the secondary locations stored: 
 ```java
    @Query("SELECT Secondary_Location FROM events GROUP BY Secondary_Location")
    List<String> getAllSecondaryLocations();
```
 - The following returns all the event types stored:
 ```java
@Query("SELECT Event_Type FROM events GROUP BY Event_Type")
    List<String> getAllEventTypes();
```
 - Finally, the filter method: 
 ```java
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
```
 - A small change, but maybe you already have it like this. I've added a conflict strategy for the insertAll method:
 ```java
@Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Events... events);
```
- EventsRepository has a couple of new methods as well, most of them are just to use the new methods introduced in EventsDao (I'll leave just one example)
 - To get all the primary locations. I'm using lambdas, but if you don't have the java version for that, you can implement this the classic way as well:
 ```java
public LiveData<List<String>> getAllPrimaryLocations() {
        MutableLiveData<List<String>> primaryLocations = new MutableLiveData<>();

        AsyncTask.execute(() -> primaryLocations.postValue(eventsDatabase.eventsDao().getAllPrimaryLocations()));

        return primaryLocations;
    }
```
 - Filter events method:
 ```java
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
```
 - I've also added a new method that retrieves all the events, sets the triggerTimeValue and resetTimeValue (the two new fields added to Events) using the trigger_Time and reset_Time, parsing these using DateFormat and inserting back into the database. This is the reason I've added the replace conflict strategy, so we override the existing items with the modified ones:
 ```java
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
```
- The ViewModel has a couple of new methods as well, but these are just using the repository methods. For example:
```java
public LiveData<List<String>> getPrimaryLocations() {
        return repository.getAllPrimaryLocations();
    }
```

#### Final touches
Finally, you'll need to add the MultiSpinner and Spinner to your Activity/Fragments layout xml as I've added it in my `activity_main.xml` and add listeners for these spinners inside your Actvity/Fragment, like I did in my `MainActivity`.
