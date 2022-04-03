package timetable.users;

import com.fasterxml.jackson.annotation.*;
import java.nio.charset.*;
import java.security.*;
import java.time.*;
import java.util.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.*;

@Document("TimeTableUsers")
public final class UserData {
    private static final MessageDigest hasher = getHashAlgorithm();

    @Id
    public transient String id;
    public final ClassData[] classes;
    public final transient LocalDateTime creationDate;
    public final transient String password;

    @PersistenceConstructor
    public UserData(String id, ClassData[] classes, LocalDateTime creationDate, String password) {
        this.id = id;
        this.classes = classes;
        this.creationDate = creationDate;
        this.password = password;
    }

    @JsonCreator
    public UserData(@JsonProperty("classes") ClassData[] classes,
                    @JsonProperty("password") String password) {

        this.classes = classes;
        this.creationDate = LocalDateTime.now();
        this.password = encodePassword(password);
    }

    private static String encodePassword(String password) {
        return Base64.getEncoder().encodeToString(hasher.digest(password.getBytes(StandardCharsets.UTF_8)));
    }

    private static MessageDigest getHashAlgorithm() {
        try {
            return MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}