package timetable.model;

import javax.json.bind.annotation.*;
import org.springframework.data.annotation.*;

public final class UserData {

    @Id
    public transient String id;
    public final ClassData[] classes;

    public UserData(String existingID, UserData data) {
        this.id = existingID;
        this.classes = data.classes;
    }

    @PersistenceConstructor
    public UserData(String id, ClassData[] classes) {
        this.id = id;
        this.classes = classes;
    }

    @JsonbCreator
    public UserData(@JsonbProperty("classes") ClassData[] classes) {
        this.classes = classes;
    }
}