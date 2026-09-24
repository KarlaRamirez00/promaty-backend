package com.promaty.rrhh.services.colaborador;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.colaborador.ColaboradorDetailDto;
import com.promaty.rrhh.dto.colaborador.ColaboradorFilterParams;
import com.promaty.rrhh.dto.colaborador.ColaboradorListDto;
import com.promaty.rrhh.dto.colaborador.CreateColaboradorDto;
import com.promaty.rrhh.dto.colaborador.UpdateColaboradorDto;
import com.promaty.rrhh.dto.shared.Action;
import com.promaty.rrhh.entity.Colaborador;
import com.promaty.rrhh.exception.ResourceNotFoundException;
import com.promaty.rrhh.repository.ColaboradorRepository;
import com.promaty.rrhh.services.colaborador.business.builder.ColaboradorQueryBuilder;
import com.promaty.rrhh.services.colaborador.business.mapper.ColaboradorMapper;
import com.promaty.rrhh.services.colaborador.business.validation.ColaboradorValidation;
import com.promaty.rrhh.services.shared.ActionsResolver;
import com.promaty.rrhh.services.shared.CurrentUserAuthorities;

@Service
public class ColaboradorServiceImpl implements ColaboradorService {

	private static final String NO_ENCONTRADO = "Colaborador no encontrado.";

	private static final Map<String, Action> REGLAS_ACCIONES = Map.of(
		"colaborador.update", Action.UPDATE,
		"colaborador.active", Action.ACTIVE
	);

	private final ColaboradorRepository colaboradorRepository;
	private final ColaboradorValidation colaboradorValidation;
	private final ActionsResolver actionsResolver;

	public ColaboradorServiceImpl(
		ColaboradorRepository colaboradorRepository,
		ColaboradorValidation colaboradorValidation,
		ActionsResolver actionsResolver
	) {
		this.colaboradorRepository = colaboradorRepository;
		this.colaboradorValidation = colaboradorValidation;
		this.actionsResolver = actionsResolver;
	}

	@Override
	@Transactional
	public Long createColaborador(CreateColaboradorDto dto) {
		colaboradorValidation.validateCreate(dto);

		Colaborador colaborador = new Colaborador();
		colaborador.setIdentificationType(dto.getIdentificationType());
		colaborador.setIdentificationNumber(dto.getIdentificationNumber());
		colaborador.setFirstName(dto.getFirstName());
		colaborador.setPaternalLastName(dto.getPaternalLastName());
		colaborador.setMaternalLastName(dto.getMaternalLastName());
		colaborador.setBirthDate(dto.getBirthDate());
		colaborador.setPersonalEmail(dto.getPersonalEmail());
		colaborador.setPhone1(dto.getPhone1());

		return colaboradorRepository.save(colaborador).getId();
	}

	@Override
	@Transactional
	public void updateColaborador(Long id, UpdateColaboradorDto dto) {
		colaboradorValidation.validateUpdate(dto);
		Colaborador existente = buscarPorId(id);

		existente.setFirstName(dto.getFirstName());
		existente.setPaternalLastName(dto.getPaternalLastName());
		existente.setMaternalLastName(dto.getMaternalLastName());
		existente.setBirthDate(dto.getBirthDate());
		existente.setPersonalEmail(dto.getPersonalEmail());
		existente.setPhone1(dto.getPhone1());

		colaboradorRepository.save(existente);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<ColaboradorListDto> listColaboradores(ColaboradorFilterParams filters, Pageable pageable) {
		Specification<Colaborador> especificacion = ColaboradorQueryBuilder.fromFilters(filters);
		List<Action> actions = actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES);
		return colaboradorRepository.findAll(especificacion, pageable)
			.map(ColaboradorMapper::toListDto)
			.map(dto -> conAcciones(dto, actions));
	}

	@Override
	@Transactional(readOnly = true)
	public ColaboradorDetailDto getColaboradorDetail(Long id) {
		return conAcciones(ColaboradorMapper.toDetailDto(buscarPorId(id)));
	}

	@Override
	@Transactional
	public ColaboradorDetailDto toggleColaboradorActive(Long id) {
		Colaborador colaborador = buscarPorId(id);
		colaborador.toggleActive();
		return conAcciones(ColaboradorMapper.toDetailDto(colaboradorRepository.save(colaborador)));
	}

	private Colaborador buscarPorId(Long id) {
		return colaboradorRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NO_ENCONTRADO));
	}

	private ColaboradorListDto conAcciones(ColaboradorListDto dto, List<Action> actions) {
		dto.setActions(actions);
		return dto;
	}

	private ColaboradorDetailDto conAcciones(ColaboradorDetailDto dto) {
		dto.setActions(actionsResolver.resolve(CurrentUserAuthorities.get(), REGLAS_ACCIONES));
		return dto;
	}
}
