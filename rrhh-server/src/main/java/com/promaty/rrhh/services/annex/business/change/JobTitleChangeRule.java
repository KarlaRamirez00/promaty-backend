package com.promaty.rrhh.services.annex.business.change;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.entity.Annex;
import com.promaty.rrhh.entity.AnnexChangeValues;
import com.promaty.rrhh.entity.AnnexTypeCode;
import com.promaty.rrhh.entity.Contract;
import com.promaty.rrhh.entity.JobTitle;
import com.promaty.rrhh.repository.JobTitleRepository;

@Component
public class JobTitleChangeRule implements AnnexChangeRule {

	private static final String FIELD_NEW_JOB_TITLE = "newJobTitleId";

	private final JobTitleRepository jobTitleRepository;

	public JobTitleChangeRule(JobTitleRepository jobTitleRepository) {
		this.jobTitleRepository = jobTitleRepository;
	}

	@Override
	public AnnexTypeCode supports() {
		return AnnexTypeCode.JOB_TITLE_CHANGE;
	}

	@Override
	public Set<AnnexChangeField> fields() {
		return Set.of(AnnexChangeField.NEW_JOB_TITLE);
	}

	@Override
	public void validate(AnnexChangeValues values, Contract contract, Map<String, String> errors) {
		Long newJobTitleId = values.getNewJobTitleId();
		if (newJobTitleId == null) {
			errors.put(FIELD_NEW_JOB_TITLE, "El nuevo cargo es obligatorio.");
			return;
		}
		Optional<JobTitle> jobTitle = jobTitleRepository.findById(newJobTitleId);
		if (jobTitle.isEmpty()) {
			errors.put(FIELD_NEW_JOB_TITLE, "El cargo indicado no existe.");
		} else if (!Boolean.TRUE.equals(jobTitle.get().getActive())) {
			errors.put(FIELD_NEW_JOB_TITLE, "El cargo indicado no está activo.");
		} else if (newJobTitleId.equals(contract.getJobTitle().getId())) {
			errors.put(FIELD_NEW_JOB_TITLE, "El nuevo cargo debe ser distinto del actual.");
		}
	}

	@Override
	public void apply(Annex annex, Contract contract) {
		annex.setPreviousJobTitle(contract.getJobTitle());
		contract.setJobTitle(annex.getNewJobTitle());
	}
}
