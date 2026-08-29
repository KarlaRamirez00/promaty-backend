package com.promaty.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.user.entity.SubModule;

public interface SubModuleRepository extends JpaRepository<SubModule, Long> {

	List<SubModule> findByIdIn(List<Long> ids);
}
