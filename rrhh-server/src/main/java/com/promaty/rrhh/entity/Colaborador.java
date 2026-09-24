package com.promaty.rrhh.entity;

import java.time.LocalDate;

import com.promaty.rrhh.entity.base.BaseDatedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "colaboradores")
public class Colaborador extends BaseDatedEntity {

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

	@Column(name = "personal_email", nullable = false)
	private String personalEmail;

	@Column(name = "phone1", nullable = false, length = 9)
	private String phone1;

	@Column(nullable = false)
	private Boolean active = true;

	public void toggleActive() {
		this.active = !this.active;
	}
}
