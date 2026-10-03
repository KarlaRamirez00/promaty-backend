package com.promaty.rrhh.dto.staff;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.AccountType;
import com.promaty.rrhh.entity.ClothingSize;
import com.promaty.rrhh.entity.IdentificationType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StaffDetailDto {

	private Long id;
	private IdentificationType identificationType;
	private String identificationNumber;
	private String firstName;
	private String paternalLastName;
	private String maternalLastName;
	private String costCenterCode;
	private LocalDate birthDate;
	private RelationSummaryDto registeredSex;
	private RelationSummaryDto maritalStatus;
	private RelationSummaryDto nationality;
	private String phone1;
	private String emergencyPhone;
	private String emergencyContactName;
	private String address;
	private RelationSummaryDto region;
	private RelationSummaryDto provincia;
	private RelationSummaryDto comuna;
	private Boolean hasChildren;
	private Integer childrenCount;
	private String personalEmail;
	private Integer shoeSize;
	private ClothingSize clothingSize;
	private RelationSummaryDto educationLevel;
	private RelationSummaryDto afp;
	private RelationSummaryDto healthSystem;
	private RelationSummaryDto bank;
	private AccountType accountType;
	private String accountNumber;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private String createdBy;
	private String updatedBy;
	private List<Action> actions;
}
