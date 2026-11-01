package com.promaty.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.promaty.user.entity.OneTimeCode;
import com.promaty.user.entity.OneTimeCodePurpose;

public interface OneTimeCodeRepository extends JpaRepository<OneTimeCode, Long> {

	Optional<OneTimeCode> findFirstByUser_IdAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
		Long userId,
		OneTimeCodePurpose purpose
	);
}
