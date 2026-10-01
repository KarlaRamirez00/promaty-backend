package com.promaty.rrhh.services.comuna;

import java.util.List;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;

public interface ComunaService {

	List<CatalogOptionDto> listOptionsByProvincia(Long provinciaId);
}
