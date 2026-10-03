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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "contract")
public class Contract extends BaseDatedEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "staff_id", nullable = false)
	private Staff staff;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "company_id", nullable = false)
	private Company company;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contract_type_id", nullable = false)
	private ContractType contractType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_title_id", nullable = false)
	private JobTitle jobTitle;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_id", nullable = false)
	private Site site;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(name = "base_salary", nullable = false)
	private BigDecimal baseSalary;

	@Column(name = "agreed_salary")
	private BigDecimal agreedSalary;

	@Column(name = "weekly_work_hours", nullable = false)
	private Integer weeklyWorkHours;

	@Column(name = "work_days", nullable = false)
	private Integer workDays;

	@Column(name = "contract_detail", length = 500)
	private String contractDetail;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meal_type_id")
	private MealType mealType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "transport_type_id")
	private TransportType transportType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "status_id", nullable = false)
	private PlatformStatus status;

	@Column(name = "name", length = 100)
	private String name;

	@Column(name = "contract_number", unique = true, length = 20)
	private String contractNumber;
}
