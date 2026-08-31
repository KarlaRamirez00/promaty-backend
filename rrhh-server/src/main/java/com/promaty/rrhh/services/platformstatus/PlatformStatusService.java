package com.promaty.rrhh.services.platformstatus;

import java.util.List;

import com.promaty.rrhh.dto.platformstatus.PlatformStatusOptionDto;

public interface PlatformStatusService {

	List<PlatformStatusOptionDto> listOptionsBySubModule(String subModule);
}
