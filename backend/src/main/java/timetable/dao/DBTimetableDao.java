package timetable.dao;

import com.mongodb.client.*;
import org.springframework.data.mongodb.core.*;
import org.springframework.stereotype.*;
import timetable.model.*;

@Repository("db")
public /*non-final*/ class DBTimetableDao implements TimetableDao {
    private static final MongoTemplate database = new MongoTemplate(MongoClients.create(System.getenv("DB_CONNECTION")), "TimeTable");
    private static final String COLLECTION_CLASSES = "Classes";

    @Override
    public UserData get(String id) {
        var inDB = database.findById(id, UserData.class, COLLECTION_CLASSES);
        return inDB == null ? null : inDB;
    }

    @Override
    public boolean update(String id, UserData data) {
        var old = database.findById(id, UserData.class, COLLECTION_CLASSES);

        if(old != null) {
            database.remove(old, COLLECTION_CLASSES);
            database.insert(new UserData(id, data), COLLECTION_CLASSES);
            return true;
        }

        return false;
    }

    @Override
    public String create(UserData data) {
        return database.insert(data, COLLECTION_CLASSES).id;
    }
}