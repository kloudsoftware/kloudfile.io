package me.probE466.persistence.repos;

import me.probE466.persistence.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by larsg on 26.09.2016.
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByKey(String key);
}
