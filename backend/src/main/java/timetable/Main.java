package timetable;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.autoconfigure.mongo.*;

@SpringBootApplication(exclude = MongoAutoConfiguration.class, scanBasePackages = { "timetable.controller", "timetable.dao" })
public /*non-final*/ class Main {
    public static final String PROD = "prod";
    public static final String DEV = "dev";

    public static final String ENVIRONMENT = PROD;

    @SuppressWarnings("resource")
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}