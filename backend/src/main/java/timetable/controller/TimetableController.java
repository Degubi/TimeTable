package timetable.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import timetable.dao.*;
import timetable.model.*;

@RequestMapping("/timetable")
@RestController
public final class TimetableController {

    private final TimetableDao timetableDao;

    public TimetableController(@Qualifier("db") TimetableDao dao) {
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
        if(id.equals("null")) {
            return new ResponseEntity<>(timetableDao.create(clazz), HttpStatus.OK);
        }
        var updated = timetableDao.update(id, clazz);

        return updated ? new ResponseEntity<>(HttpStatus.OK)
                       : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}