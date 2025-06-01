package br.com.gustavo.catalog.repositories;

import br.com.gustavo.catalog.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
