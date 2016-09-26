package me.probE466.persistence.repos;

import me.probE466.persistence.entities.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by larsg on 25.09.2016.
 */
public interface FileRepository extends JpaRepository<File, Integer> {
    // TODO: 26.09.2016 Verify this
    Optional<File> findByHash(String hash);
}
