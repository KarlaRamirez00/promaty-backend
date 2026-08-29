package com.promaty.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.promaty.user.entity.User;
import com.promaty.user.repository.projection.RoleUserCount;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

	boolean existsByEmail(String email);

	Optional<User> findByEmail(String email);

	List<User> findByRole_Id(Long roleId);

	long countByRole_Id(Long roleId);

	@Query("SELECT u.role.id AS roleId, COUNT(u) AS total FROM User u GROUP BY u.role.id")
	List<RoleUserCount> countUsersGroupedByRole();
}
