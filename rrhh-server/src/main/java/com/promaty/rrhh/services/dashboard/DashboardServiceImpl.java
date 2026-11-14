package com.promaty.rrhh.services.dashboard;

import static com.promaty.rrhh.services.dashboard.business.builder.DashboardQueryBuilder.dateBetween;
import static com.promaty.rrhh.services.dashboard.business.builder.DashboardQueryBuilder.fetchingStaffAndProject;
import static com.promaty.rrhh.services.dashboard.business.builder.DashboardQueryBuilder.hasStatus;
import static com.promaty.rrhh.services.dashboard.business.builder.DashboardQueryBuilder.hasStatusIn;
import static com.promaty.rrhh.services.dashboard.business.builder.DashboardQueryBuilder.latestContractOfBlockedStaff;
import static com.promaty.rrhh.services.dashboard.business.builder.DashboardQueryBuilder.ofEntityType;
import static com.promaty.rrhh.services.dashboard.business.builder.DashboardQueryBuilder.staffBirthdayInMonth;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.dashboard.BirthdayDto;
import com.promaty.rrhh.dto.dashboard.DashboardSummaryDto;
import com.promaty.rrhh.dto.dashboard.PendingRequestsDto;
import com.promaty.rrhh.dto.dashboard.ProjectProgressDto;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.Project;
import com.promaty.rrhh.entity.Request;
import com.promaty.rrhh.entity.RequestEntityType;
import com.promaty.rrhh.entity.SettingKey;
import com.promaty.rrhh.entity.Staff;
import com.promaty.rrhh.entity.Termination;
import com.promaty.rrhh.repository.ContractRepository;
import com.promaty.rrhh.repository.ProjectRepository;
import com.promaty.rrhh.repository.RequestRepository;
import com.promaty.rrhh.repository.StaffRepository;
import com.promaty.rrhh.repository.TerminationRepository;
import com.promaty.rrhh.services.contract.business.builder.ContractExpirationSpecification;
import com.promaty.rrhh.services.dashboard.business.DashboardScope;
import com.promaty.rrhh.services.dashboard.business.ProjectProgressCalculator;
import com.promaty.rrhh.services.dashboard.business.mapper.DashboardMapper;
import com.promaty.rrhh.services.setting.SettingProvider;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;

@Service
public class DashboardServiceImpl implements DashboardService {

	private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Santiago");

	private static final String PERMISSION_STAFF_READ = "staff.read";
	private static final String PERMISSION_CONTRACT_READ = "contract.read";
	private static final String PERMISSION_TERMINATION_READ = "termination.read";
	private static final String PERMISSION_CONTRACT_APPROVE = "contract.approve";
	private static final String PERMISSION_CONTRACT_VALIDATE = "contract.validate";
	private static final String PERMISSION_TERMINATION_APPROVE = "termination.approve";
	private static final String PERMISSION_TERMINATION_VALIDATE = "termination.validate";
	private static final String PERMISSION_ANNEX_APPROVE = "annex.approve";
	private static final String PERMISSION_ANNEX_VALIDATE = "annex.validate";

	private static final String CODE_ACTIVE = "ACTIVE";
	private static final String CODE_BLOCKED = "BLOCKED";
	private static final String CODE_APPROVED = "APPROVED";
	private static final String CODE_IN_PROGRESS = "IN_PROGRESS";
	private static final String CODE_PENDING_APPROVAL = "PENDING_APPROVAL";
	private static final String CODE_PENDING_VALIDATION = "PENDING_VALIDATION";
	private static final List<String> CODES_SCHEDULED_HIRE =
		List.of(CODE_ACTIVE, CODE_PENDING_APPROVAL, CODE_PENDING_VALIDATION);

	private static final String FIELD_START_DATE = "startDate";
	private static final String FIELD_TERMINATION_DATE = "terminationDate";
	private static final String FIELD_COST_CENTER_CODE = "costCenterCode";

	private final ContractRepository contractRepository;
	private final StaffRepository staffRepository;
	private final TerminationRepository terminationRepository;
	private final RequestRepository requestRepository;
	private final ProjectRepository projectRepository;
	private final SettingProvider settingProvider;

	public DashboardServiceImpl(
		ContractRepository contractRepository,
		StaffRepository staffRepository,
		TerminationRepository terminationRepository,
		RequestRepository requestRepository,
		ProjectRepository projectRepository,
		SettingProvider settingProvider
	) {
		this.contractRepository = contractRepository;
		this.staffRepository = staffRepository;
		this.terminationRepository = terminationRepository;
		this.requestRepository = requestRepository;
		this.projectRepository = projectRepository;
		this.settingProvider = settingProvider;
	}

	@Override
	@Transactional(readOnly = true)
	public DashboardSummaryDto getSummary(Long projectId) {
		DashboardScope scope = DashboardScope.of(projectId);
		Set<String> authorities = CurrentUserAuthorities.get();
		LocalDate today = LocalDate.now(BUSINESS_ZONE);

		DashboardSummaryDto summary = new DashboardSummaryDto();
		summary.setPendingRequests(resolvePendingRequests(scope, authorities));
		if (authorities.contains(PERMISSION_STAFF_READ)) {
			summary.setActiveStaff(countContracts(scope, hasStatus(CODE_ACTIVE)));
			summary.setBlockedStaff(countBlockedStaff(scope));
		}
		if (authorities.contains(PERMISSION_CONTRACT_READ)) {
			fillContractIndicators(summary, scope, today);
		}
		if (authorities.contains(PERMISSION_TERMINATION_READ)) {
			summary.setTerminationsThisMonth(countTerminationsOfMonth(scope, YearMonth.from(today)));
		}
		return summary;
	}

	@Override
	@Transactional(readOnly = true)
	public List<BirthdayDto> listBirthdaysOfCurrentMonth(Long projectId) {
		DashboardScope scope = DashboardScope.of(projectId);
		int currentMonth = LocalDate.now(BUSINESS_ZONE).getMonthValue();
		Specification<Contract> specification = scope.<Contract>onProject()
			.and(hasStatus(CODE_ACTIVE))
			.and(staffBirthdayInMonth(currentMonth))
			.and(fetchingStaffAndProject());
		return contractRepository.findAll(specification).stream()
			.map(DashboardMapper::toBirthdayDto)
			.sorted(Comparator.comparing(BirthdayDto::getDay).thenComparing(BirthdayDto::getFullName))
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProjectProgressDto> listProjectProgress(Long projectId) {
		DashboardScope scope = DashboardScope.of(projectId);
		LocalDate today = LocalDate.now(BUSINESS_ZONE);
		Specification<Project> specification = scope.<Project>onProjectId().and(hasStatus(CODE_IN_PROGRESS));
		return projectRepository.findAll(specification, Sort.by(FIELD_COST_CENTER_CODE)).stream()
			.map(project -> ProjectProgressCalculator.calculate(project, today))
			.toList();
	}

	private void fillContractIndicators(DashboardSummaryDto summary, DashboardScope scope, LocalDate today) {
		int expiringDays = settingProvider.getInt(SettingKey.EXPIRING_CONTRACT_DAYS);
		YearMonth thisMonth = YearMonth.from(today);
		YearMonth nextMonth = thisMonth.plusMonths(1);

		summary.setExpiringContractsDays(expiringDays);
		summary.setExpiringContracts(countContracts(scope, ContractExpirationSpecification.expiringSoon(today, expiringDays)));
		summary.setHiresThisMonth(countContracts(scope, startingIn(thisMonth).and(hasStatus(CODE_ACTIVE))));
		summary.setHiresNextMonth(countContracts(scope, startingIn(nextMonth).and(hasStatusIn(CODES_SCHEDULED_HIRE))));
	}

	private Specification<Contract> startingIn(YearMonth month) {
		return dateBetween(FIELD_START_DATE, month.atDay(1), month.atEndOfMonth());
	}

	private long countContracts(DashboardScope scope, Specification<Contract> condition) {
		return contractRepository.count(scope.<Contract>onProject().and(condition));
	}

	private long countBlockedStaff(DashboardScope scope) {
		if (scope.coversEveryProject()) {
			Specification<Staff> blocked = hasStatus(CODE_BLOCKED);
			return staffRepository.count(blocked);
		}
		return countContracts(scope, latestContractOfBlockedStaff());
	}

	private long countTerminationsOfMonth(DashboardScope scope, YearMonth month) {
		Specification<Termination> specification = scope.<Termination>onProject()
			.and(hasStatus(CODE_APPROVED))
			.and(dateBetween(FIELD_TERMINATION_DATE, month.atDay(1), month.atEndOfMonth()));
		return terminationRepository.count(specification);
	}

	private PendingRequestsDto resolvePendingRequests(DashboardScope scope, Set<String> authorities) {
		Long contract = countPendingRequests(
			scope, authorities, RequestEntityType.CONTRACT, PERMISSION_CONTRACT_APPROVE, PERMISSION_CONTRACT_VALIDATE
		);
		Long termination = countPendingRequests(
			scope, authorities, RequestEntityType.TERMINATION, PERMISSION_TERMINATION_APPROVE, PERMISSION_TERMINATION_VALIDATE
		);
		Long annex = countPendingRequests(
			scope, authorities, RequestEntityType.ANNEX, PERMISSION_ANNEX_APPROVE, PERMISSION_ANNEX_VALIDATE
		);
		boolean noneVisible = contract == null && termination == null && annex == null;
		return noneVisible ? null : new PendingRequestsDto(contract, termination, annex);
	}

	private Long countPendingRequests(
		DashboardScope scope,
		Set<String> authorities,
		RequestEntityType entityType,
		String approvePermission,
		String validatePermission
	) {
		boolean canApprove = authorities.contains(approvePermission);
		boolean canValidate = authorities.contains(validatePermission);
		if (!canApprove && !canValidate) {
			return null;
		}
		long pending = 0;
		if (canApprove) {
			pending += countRequests(scope, entityType, CODE_PENDING_APPROVAL);
		}
		if (canValidate) {
			pending += countRequests(scope, entityType, CODE_PENDING_VALIDATION);
		}
		return pending;
	}

	private long countRequests(DashboardScope scope, RequestEntityType entityType, String statusCode) {
		Specification<Request> specification = scope.<Request>onProject()
			.and(ofEntityType(entityType))
			.and(hasStatus(statusCode));
		return requestRepository.count(specification);
	}
}
