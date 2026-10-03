package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.Site;

public interface SiteRepository extends JpaRepository<Site, Long> {

	List<Site> findByActiveTrueOrderByName();
}
