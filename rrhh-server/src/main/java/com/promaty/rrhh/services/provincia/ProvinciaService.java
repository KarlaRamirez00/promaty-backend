package com.promaty.rrhh.services.provincia;

import java.util.List;

import com.promaty.rrhh.dto.shared.CatalogOptionDto;

public interface ProvinciaService {

	List<CatalogOptionDto> listOptionsByRegion(Long regionId);
}
