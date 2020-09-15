package timetable.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import timetable.*;
import timetable.dao.*;
import timetable.model.*;

@RequestMapping("/timetable")
@RestController
public final class TimetableController {

    private final TimetableDao timetableDao;

    public TimetableController(@Qualifier(Main.ENVIRONMENT) TimetableDao dao) {
        this.timetableDao = dao;
    }

    @GetMapping
    public ResponseEntity<UserData> get(@RequestParam String id) {
        var fromDB = timetableDao.get(id);

        return fromDB == null ? new ResponseEntity<>(HttpStatus.BAD_REQUEST)
                              : new ResponseEntity<>(fromDB, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> createOrUpdate(@RequestParam String id, @RequestBody UserData clazz) {
        return id.equals("null") ? new ResponseEntity<>(timetableDao.create(clazz), HttpStatus.OK)
                                 : new ResponseEntity<>(timetableDao.update(id, clazz));
    }
}