package com.promaty.rrhh.services.shared;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Genera un code estable a partir de un name editable por el usuario, para catálogos donde el code
 * es solo un identificador interno (nunca se muestra en UI) y pedírselo como input confundiría al
 * usuario. No garantiza unicidad por sí solo — quien lo use debe resolver colisiones (ver
 * NonRehireReasonValidation).
 *
 * El code queda en español (idioma de name), a diferencia de los code en inglés sembrados por
 * data.sql para otros catálogos (Bank, Afp, etc.) y de los 7 NonRehireReason iniciales. Lo ideal
 * sería traducir para mantener el estándar, pero agregar traducción automática solo para un campo
 * que nunca se expone al usuario final es sobreingeniería para este MVP.
 *
 * Se descartan preposiciones, artículos y las contracciones al/del para que el code sea breve — su
 * ausencia no afecta la unicidad porque esa la resuelve NonRehireReasonValidation con un sufijo
 * numérico, no el texto del code.
 */
public final class SlugCodeGenerator {

	private static final Pattern DIACRITIC_MARKS = Pattern.compile("\\p{M}");
	private static final Pattern GUION_EN_BORDE = Pattern.compile("^_+|_+$");

	private static final Set<String> PALABRAS_VACIAS = Set.of(
		"A", "ANTE", "BAJO", "CABE", "CON", "CONTRA", "DE", "DESDE", "DURANTE", "EN", "ENTRE",
		"HACIA", "HASTA", "MEDIANTE", "PARA", "POR", "SEGUN", "SIN", "SO", "SOBRE", "TRAS", "VERSUS",
		"VIA", "EL", "LA", "LOS", "LAS", "UN", "UNA", "UNOS", "UNAS", "AL", "DEL", "O", "QUE", "MAS"
	);

	private SlugCodeGenerator() {
	}

	public static String fromName(String name, int maxLength) {
		String sinTildes = Normalizer.normalize(name.toUpperCase(), Normalizer.Form.NFD);
		String sinDiacriticos = DIACRITIC_MARKS.matcher(sinTildes).replaceAll("");
		List<String> palabrasSignificativas = Arrays.stream(sinDiacriticos.split("[^A-Z0-9]+"))
			.filter(palabra -> !palabra.isBlank() && !PALABRAS_VACIAS.contains(palabra))
			.collect(Collectors.toList());
		return truncarPorPalabraCompleta(palabrasSignificativas, maxLength);
	}

	private static String truncarPorPalabraCompleta(List<String> palabras, int maxLength) {
		StringBuilder slug = new StringBuilder();
		for (String palabra : palabras) {
			String candidato = slug.isEmpty() ? palabra : slug + "_" + palabra;
			if (candidato.length() > maxLength) {
				break;
			}
			slug = new StringBuilder(candidato);
		}
		if (slug.isEmpty() && !palabras.isEmpty()) {
			return palabras.get(0).substring(0, maxLength);
		}
		return GUION_EN_BORDE.matcher(slug).replaceAll("");
	}
}
