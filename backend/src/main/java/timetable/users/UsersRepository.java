package timetable.users;

import org.springframework.data.mongodb.repository.*;

public interface UsersRepository extends MongoRepository<UserData, String> {}