package timetable.users;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/timetable")
public final class UsersController {

    private final UsersRepository users;

    public UsersController(UsersRepository users) {
        this.users = users;
    }

    @GetMapping
    public ResponseEntity<UserData> get(@RequestParam String id) {
        var user = users.findById(id);

        return user.map(k -> new ResponseEntity<>(k, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PostMapping
    public ResponseEntity<String> createOrUpdate(@RequestParam(required = false) String id, @RequestBody UserData newUserData) {
        if(id == null) {
            return new ResponseEntity<>(users.insert(newUserData).id, HttpStatus.OK);
        }

        var existingUser = users.findById(id).orElse(null);

        if(existingUser != null) {
            if(!existingUser.password.equals(newUserData.password)) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            newUserData.id = id;
            users.save(newUserData);
            return new ResponseEntity<>(null, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}