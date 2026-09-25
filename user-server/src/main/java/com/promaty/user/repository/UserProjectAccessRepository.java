package com.promaty.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.promaty.user.entity.UserProjectAccess;

public interface UserProjectAccessRepository extends JpaRepository<UserProjectAccess, Long> {

	@Query("SELECT upa.projectId FROM UserProjectAccess upa WHERE upa.user.id = :userId")
	List<Long> findProjectIdsByUserId(Long userId);
}
