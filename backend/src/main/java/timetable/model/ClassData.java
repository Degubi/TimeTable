package timetable.model;

import java.time.*;
import javax.json.bind.annotation.*;
import org.springframework.data.annotation.*;

public final class ClassData {

    public final String day;
    public final LocalTime startTime;
    public final LocalTime endTime;
    public final String name;
    public final String type;
    public final String room;
    public final boolean unImportant;

    @JsonbCreator
    @PersistenceConstructor
    public ClassData(@JsonbProperty("day") String day,
                     @JsonbProperty("startTime") LocalTime startTime,
                     @JsonbProperty("endTime") LocalTime endTime,
                     @JsonbProperty("name") String name,
                     @JsonbProperty("type") String type,
                     @JsonbProperty("room") String room,
                     @JsonbProperty("unImportant") boolean unImportant) {

        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.name = name;
        this.type = type;
        this.room = room;
        this.unImportant = unImportant;
    }
}