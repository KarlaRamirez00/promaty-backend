package com.promaty.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.user.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {

	List<Permission> findByIdIn(List<Long> ids);
}
