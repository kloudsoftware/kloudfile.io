package io.kloudfile.persistence.repos;

import io.kloudfile.persistence.entities.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Integer> {
    Optional<Url> findByUrl(String url);

}
