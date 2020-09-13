package timetable.dao;

import java.util.*;
import org.springframework.stereotype.*;
import timetable.*;
import timetable.model.*;

@Repository(Main.DEV)
public /*non-final*/ class TestTimetableDao implements TimetableDao {

    private final HashMap<String, UserData> storage = new HashMap<>();

    @Override
    public UserData get(String id) {
        return storage.get(id);
    }

    @Override
    public boolean update(String id, UserData data) {
        if(storage.containsKey(id)) {
            storage.put(id, data);
            return true;
        }
        return false;
    }

    @Override
    public String create(UserData data) {
        var id = "generated" + storage.size();
        storage.put(id, data);
        return id;
    }
}