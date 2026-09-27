package com.promaty.rrhh.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.rrhh.entity.Approval;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {

	List<Approval> findByRequest_IdOrderByCreatedAtAsc(Long requestId);
}
