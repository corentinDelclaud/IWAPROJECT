package iwaproject.user_microservice.repository;

import iwaproject.user_microservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    // Find non-deleted user by email
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    // Check if username exists (for validation)
    boolean existsByUsername(String username);

    // Check if email exists (for validation)
    boolean existsByEmail(String email);

    // Check if non-deleted user exists by id
    boolean existsByIdAndDeletedAtIsNull(String id);

    // Check if non-deleted user exists by email
    boolean existsByEmailAndDeletedAtIsNull(String email);

    // Find all non-deleted users with pagination
    Page<User> findAllByDeletedAtIsNull(Pageable pageable);

    // Search users by username or email (case-insensitive) with pagination
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseAndDeletedAtIsNull(
            String username, String email, Pageable pageable);

    // Find multiple users by IDs
    List<User> findAllByIdInAndDeletedAtIsNull(List<String> ids);

    // Count non-deleted users
    long countByDeletedAtIsNull();

    // Count deleted users
    long countByDeletedAtIsNotNull();

    // Count users created after a certain date
    long countByCreatedAtAfterAndDeletedAtIsNull(LocalDateTime date);
}
