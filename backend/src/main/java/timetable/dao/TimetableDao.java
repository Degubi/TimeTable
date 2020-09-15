package timetable.dao;

import org.springframework.http.*;
import timetable.model.*;

public interface TimetableDao {

    UserData get(String id);
    HttpStatus update(String id, UserData data);
    String create(UserData data);
}