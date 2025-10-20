package iwaproject.user_microservice.repository;

import iwaproject.user_microservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // Find user by username
    Optional<User> findByUsername(String username);

    // Find user by email
    Optional<User> findByEmail(String email);

    // Find non-deleted user by id
    Optional<User> findByIdAndDeletedAtIsNull(String id);

    // Find non-deleted user by username
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    // Check if username exists (for validation)
    boolean existsByUsername(String username);

    // Check if email exists (for validation)
    boolean existsByEmail(String email);
}
