package io.kloudfile.persistence.repos;

import io.kloudfile.persistence.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUserKey(String userKey);
}
