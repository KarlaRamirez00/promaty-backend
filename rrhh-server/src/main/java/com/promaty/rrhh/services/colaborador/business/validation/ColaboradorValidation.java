package com.promaty.rrhh.services.colaborador.business.validation;

import java.time.LocalDate;
import java.time.Period;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.promaty.rrhh.dto.colaborador.CreateColaboradorDto;
import com.promaty.rrhh.dto.colaborador.UpdateColaboradorDto;
import com.promaty.rrhh.entity.Colaborador;
import com.promaty.rrhh.entity.IdentificationType;
import com.promaty.rrhh.exception.BusinessValidationException;
import com.promaty.rrhh.repository.ColaboradorRepository;

@Component
public class ColaboradorValidation {

	private static final String MENSAJE_VALIDACION = "La validacion fallo para uno o mas campos.";
	private static final int EDAD_MINIMA = 18;

	private final ColaboradorRepository colaboradorRepository;

	public ColaboradorValidation(ColaboradorRepository colaboradorRepository) {
		this.colaboradorRepository = colaboradorRepository;
	}

	public void validateCreate(CreateColaboradorDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		if (colaboradorRepository.findByIdentificationNumber(dto.getIdentificationNumber()).isPresent()) {
			errores.put("identificationNumber", "Ya existe un colaborador con este numero de identificacion.");
		}
		if (dto.getIdentificationType() == IdentificationType.RUT
			&& !esRutValido(dto.getIdentificationNumber())) {
			errores.put("identificationNumber", "El RUT ingresado no es valido.");
		}
		validarMayoriaDeEdad(dto.getBirthDate(), errores);

		lanzarSiHayErrores(errores);
	}

	public void validateUpdate(UpdateColaboradorDto dto) {
		Map<String, String> errores = new LinkedHashMap<>();

		validarMayoriaDeEdad(dto.getBirthDate(), errores);

		lanzarSiHayErrores(errores);
	}

	private void validarMayoriaDeEdad(LocalDate birthDate, Map<String, String> errores) {
		if (birthDate != null && Period.between(birthDate, LocalDate.now()).getYears() < EDAD_MINIMA) {
			errores.put("birthDate", "El colaborador debe ser mayor de " + EDAD_MINIMA + " anios.");
		}
	}

	private boolean esRutValido(String rut) {
		String limpio = rut.replace(".", "").replace("-", "").toUpperCase();
		if (limpio.length() < 2) {
			return false;
		}
		String cuerpo = limpio.substring(0, limpio.length() - 1);
		String digitoVerificador = limpio.substring(limpio.length() - 1);
		if (!cuerpo.chars().allMatch(Character::isDigit)) {
			return false;
		}

		int suma = 0;
		int multiplicador = 2;
		for (int i = cuerpo.length() - 1; i >= 0; i--) {
			suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplicador;
			multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
		}
		int resto = 11 - (suma % 11);
		String esperado = switch (resto) {
			case 11 -> "0";
			case 10 -> "K";
			default -> String.valueOf(resto);
		};
		return esperado.equals(digitoVerificador);
	}

	private void lanzarSiHayErrores(Map<String, String> errores) {
		if (!errores.isEmpty()) {
			throw new BusinessValidationException(MENSAJE_VALIDACION, errores);
		}
	}
}
