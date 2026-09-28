package com.promaty.rrhh.entity;

import java.time.LocalDate;

import com.promaty.rrhh.entity.base.BaseDatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "staff")
public class Staff extends BaseDatedEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "identification_type", nullable = false, length = 20)
	private IdentificationType identificationType;

	@Column(name = "identification_number", nullable = false, unique = true, length = 20)
	private String identificationNumber;

	@Column(name = "first_name", nullable = false, length = 100)
	private String firstName;

	@Column(name = "paternal_last_name", nullable = false, length = 100)
	private String paternalLastName;

	@Column(name = "maternal_last_name", nullable = false, length = 100)
	private String maternalLastName;

	@Column(name = "birth_date", nullable = false)
	private LocalDate birthDate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "registered_sex_id", nullable = false)
	private RegisteredSex registeredSex;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "marital_status_id", nullable = false)
	private MaritalStatus maritalStatus;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nationality_id", nullable = false)
	private Nationality nationality;

	@Column(name = "phone1", nullable = false, length = 9)
	private String phone1;

	@Column(name = "emergency_phone", nullable = false, length = 9)
	private String emergencyPhone;

	@Column(name = "emergency_contact_name", nullable = false, length = 150)
	private String emergencyContactName;

	@Column(name = "address", nullable = false, length = 200)
	private String address;

	@Column(name = "city", nullable = false, length = 100)
	private String city;

	@Column(name = "has_children", nullable = false)
	private Boolean hasChildren = false;

	@Column(name = "children_count")
	private Integer childrenCount;

	@Column(name = "personal_email", nullable = false)
	private String personalEmail;

	@Column(name = "shoe_size", nullable = false)
	private Integer shoeSize;

	@Enumerated(EnumType.STRING)
	@Column(name = "clothing_size", nullable = false, length = 5)
	private ClothingSize clothingSize;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "education_level_id", nullable = false)
	private EducationLevel educationLevel;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "afp_id", nullable = false)
	private Afp afp;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "health_system_id", nullable = false)
	private HealthSystem healthSystem;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bank_id", nullable = false)
	private Bank bank;

	@Enumerated(EnumType.STRING)
	@Column(name = "account_type", nullable = false, length = 20)
	private AccountType accountType;

	@Column(name = "account_number", nullable = false, length = 30)
	private String accountNumber;
}
