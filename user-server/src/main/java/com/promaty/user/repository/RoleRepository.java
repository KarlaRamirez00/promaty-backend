package com.promaty.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.user.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
