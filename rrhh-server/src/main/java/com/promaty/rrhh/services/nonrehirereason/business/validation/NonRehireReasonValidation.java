package com.promaty.rrhh.services.nonrehirereason.business.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.nonrehirereason.CreateNonRehireReasonDto;
import com.promaty.rrhh.dto.nonrehirereason.UpdateNonRehireReasonDto;
import com.promaty.rrhh.entity.NonRehireReason;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.NonRehireReasonRepository;
import com.promaty.rrhh.services.shared.SlugCodeGenerator;

@Component
public class NonRehireReasonValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";
	private static final int LARGO_MAXIMO_CODIGO = 40;

	private final NonRehireReasonRepository nonRehireReasonRepository;

	public NonRehireReasonValidation(NonRehireReasonRepository nonRehireReasonRepository) {
		this.nonRehireReasonRepository = nonRehireReasonRepository;
	}

	public void validateCreate(CreateNonRehireReasonDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();
		validarNombreDuplicado(errores, dto.getName(), null);
		lanzarSiHayErrores(errores);
	}

	public void validateUpdate(Long id, UpdateNonRehireReasonDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();
		validarNombreDuplicado(errores, dto.getName(), id);
		lanzarSiHayErrores(errores);
	}

	/**
	 * code es un identificador interno, nunca visible en UI: se genera desde name en vez de pedirlo
	 * como input (evita confundir al usuario con un campo que no sabría cómo llenar).
	 */
	public String resolveCode(String name, Long idActual) {
		String base = SlugCodeGenerator.fromName(name, LARGO_MAXIMO_CODIGO);
		String candidato = base;
		int sufijo = 2;
		while (colisionaConOtroRegistro(candidato, idActual)) {
			String sufijoTexto = "_" + sufijo;
			int largoBase = Math.min(base.length(), LARGO_MAXIMO_CODIGO - sufijoTexto.length());
			candidato = base.substring(0, largoBase) + sufijoTexto;
			sufijo++;
		}
		return candidato;
	}

	private boolean colisionaConOtroRegistro(String code, Long idActual) {
		Optional<NonRehireReason> existente = nonRehireReasonRepository.findByCode(code);
		return existente.isPresent() && !Objects.equals(existente.get().getId(), idActual);
	}

	/**
	 * idActual es null en create (ningún registro propio que excluir todavía) y el id real en update
	 * (excluye el propio registro del chequeo de duplicado). Objects.equals es necesario porque ambos
	 * lados pueden ser null sin que eso signifique "es el mismo registro".
	 */
	private void validarNombreDuplicado(Map<String, String> errores, String name, Long idActual) {
		Optional<NonRehireReason> existente = nonRehireReasonRepository.findByName(name);
		if (existente.isPresent() && !Objects.equals(existente.get().getId(), idActual)) {
			errores.put("name", "Ya existe un motivo de no recontratación con este nombre.");
		}
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
