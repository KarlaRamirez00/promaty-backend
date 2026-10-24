package com.promaty.rrhh.entity;

import java.time.LocalDate;

import com.promaty.rrhh.entity.base.BaseDatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "termination")
public class Termination extends BaseDatedEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "staff_id", nullable = false, updatable = false)
	private Staff staff;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contract_id", nullable = false, updatable = false)
	private Contract contract;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false, updatable = false)
	private Project project;

	@Column(name = "termination_date", nullable = false)
	private LocalDate terminationDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "legal_termination_cause_id", nullable = false)
	private LegalTerminationCause legalTerminationCause;

	@Column(name = "definitive_termination", nullable = false)
	private Boolean definitiveTermination;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "non_rehire_reason_id")
	private NonRehireReason nonRehireReason;

	@Column(length = 500)
	private String notes;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "status_id", nullable = false)
	private PlatformStatus status;

	@Column(name = "rex_termination_number", unique = true, length = 20)
	private String rexTerminationNumber;

	@Version
	@Column(nullable = false)
	private Long version;
}
