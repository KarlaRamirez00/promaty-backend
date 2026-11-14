package com.promaty.rrhh.entity;

import java.math.BigDecimal;
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
@Table(name = "annex")
public class Annex extends BaseDatedEntity implements AnnexChangeValues {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "staff_id", nullable = false, updatable = false)
	private Staff staff;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contract_id", nullable = false, updatable = false)
	private Contract contract;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false, updatable = false)
	private Project project;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "annex_type_id", nullable = false, updatable = false)
	private AnnexType annexType;

	@Column(name = "effective_date", nullable = false)
	private LocalDate effectiveDate;

	@Column(nullable = false, length = 500)
	private String description;

	@Column(name = "new_end_date")
	private LocalDate newEndDate;

	@Column(name = "new_base_salary")
	private BigDecimal newBaseSalary;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "new_job_title_id")
	private JobTitle newJobTitle;

	@Column(name = "new_weekly_work_hours")
	private Integer newWeeklyWorkHours;

	@Column(name = "new_work_days")
	private Integer newWorkDays;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "new_site_id")
	private Site newSite;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "previous_contract_type_id")
	private ContractType previousContractType;

	@Column(name = "previous_end_date")
	private LocalDate previousEndDate;

	@Column(name = "previous_base_salary")
	private BigDecimal previousBaseSalary;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "previous_job_title_id")
	private JobTitle previousJobTitle;

	@Column(name = "previous_weekly_work_hours")
	private Integer previousWeeklyWorkHours;

	@Column(name = "previous_work_days")
	private Integer previousWorkDays;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "previous_site_id")
	private Site previousSite;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "status_id", nullable = false)
	private PlatformStatus status;

	@Column(name = "rex_annex_number", unique = true, length = 30)
	private String rexAnnexNumber;

	@Version
	@Column(nullable = false)
	private Long version;

	@Override
	public Long getNewJobTitleId() {
		return newJobTitle == null ? null : newJobTitle.getId();
	}

	@Override
	public Long getNewSiteId() {
		return newSite == null ? null : newSite.getId();
	}

	public AnnexTypeCode getTypeCode() {
		return annexType.getCode();
	}
}
