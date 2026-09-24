package com.promaty.rrhh.services.colaborador;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.promaty.rrhh.dto.colaborador.ColaboradorDetailDto;
import com.promaty.rrhh.dto.colaborador.ColaboradorFilterParams;
import com.promaty.rrhh.dto.colaborador.ColaboradorListDto;
import com.promaty.rrhh.dto.colaborador.CreateColaboradorDto;
import com.promaty.rrhh.dto.colaborador.UpdateColaboradorDto;

public interface ColaboradorService {

	Long createColaborador(CreateColaboradorDto dto);

	void updateColaborador(Long id, UpdateColaboradorDto dto);

	Page<ColaboradorListDto> listColaboradores(ColaboradorFilterParams filters, Pageable pageable);

	ColaboradorDetailDto getColaboradorDetail(Long id);

	ColaboradorDetailDto toggleColaboradorActive(Long id);
}
