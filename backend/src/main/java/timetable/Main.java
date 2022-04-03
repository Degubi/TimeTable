package timetable;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;

@SpringBootApplication
public /*non-final*/ class Main {

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}