package timetable.users;

import com.fasterxml.jackson.annotation.*;
import java.time.*;
import org.springframework.data.annotation.*;

public final class ClassData {

    public final String day;
    public final LocalTime startTime;
    public final LocalTime endTime;
    public final String name;
    public final String type;
    public final String room;
    public final boolean unImportant;

    @JsonCreator
    @PersistenceConstructor
    public ClassData(@JsonProperty("day") String day,
                     @JsonProperty("startTime") LocalTime startTime,
                     @JsonProperty("endTime") LocalTime endTime,
                     @JsonProperty("name") String name,
                     @JsonProperty("type") String type,
                     @JsonProperty("room") String room,
                     @JsonProperty("unImportant") boolean unImportant) {

        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.name = name;
        this.type = type;
        this.room = room;
        this.unImportant = unImportant;
    }
}