package com.promaty.user.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.promaty.user.entity.LoginAttempt;

import jakarta.persistence.LockModeType;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {

	// El DO UPDATE toma el bloqueo de la fila al instante y renueva updatedAt, asi la limpieza programada no la borra a mitad del login.
	@Modifying
	@Query(value = """
		INSERT INTO login_attempts (email, failed_attempts, updated_at)
		VALUES (:email, 0, :now)
		ON CONFLICT (email) DO UPDATE SET updated_at = EXCLUDED.updated_at
		""", nativeQuery = true)
	void touch(@Param("email") String email, @Param("now") LocalDateTime now);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT a FROM LoginAttempt a WHERE a.email = :email")
	Optional<LoginAttempt> findByEmailForUpdate(@Param("email") String email);

	@Modifying
	@Query("DELETE FROM LoginAttempt a WHERE a.email = :email")
	void deleteByEmail(@Param("email") String email);

	@Modifying
	@Query("DELETE FROM LoginAttempt a WHERE a.updatedAt < :cutoff AND (a.lockedUntil IS NULL OR a.lockedUntil < :now)")
	int deleteInactive(@Param("cutoff") LocalDateTime cutoff, @Param("now") LocalDateTime now);
}
