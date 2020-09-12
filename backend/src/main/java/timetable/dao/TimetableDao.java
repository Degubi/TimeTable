package timetable.dao;

import timetable.model.*;

public interface TimetableDao {

    UserData get(String id);
    boolean update(String id, UserData data);
    String create(UserData data);
}