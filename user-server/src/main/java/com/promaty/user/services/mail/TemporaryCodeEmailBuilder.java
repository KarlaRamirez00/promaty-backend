package com.promaty.user.services.mail;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

import com.promaty.user.entity.OneTimeCodePurpose;
import com.promaty.user.services.credential.TemporaryCodeIssuedEvent;

@Component
public class TemporaryCodeEmailBuilder {

	private static final String TEMPLATE_PATH = "mail/temporary-code.html";
	private static final String PNG = "image/png";
	private static final String LOGO_LIGHT_ID = "promaty-logo-light";
	private static final String LOGO_DARK_ID = "promaty-logo-dark";
	private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{(\\w+)}}");

	private static final String STEPS_TEMPLATE = """
		<tr>
		  <td class="text-main" style="padding:0 32px 24px 32px;color:#1f2937;font-size:14px;line-height:21px;">
		    <p style="margin:0 0 8px 0;font-weight:bold;">Cómo continuar</p>
		    <ol style="margin:0;padding-left:20px;">{{items}}</ol>
		  </td>
		</tr>
		""";

	private record Copy(String subject, String heading, String intro, List<String> steps) {
	}

	private final String template;
	private final List<InlineImage> logos = List.of(
		new InlineImage(LOGO_LIGHT_ID, new ClassPathResource("mail/logo-light.png"), PNG),
		new InlineImage(LOGO_DARK_ID, new ClassPathResource("mail/logo-dark.png"), PNG));
	private final long validMinutes;

	public TemporaryCodeEmailBuilder(@Value("${one-time-code.expiration-minutes}") long validMinutes) {
		this.template = loadTemplate();
		this.validMinutes = validMinutes;
	}

	public EmailMessage build(TemporaryCodeIssuedEvent event) {
		Copy copy = copyFor(event.purpose());
		Map<String, String> values = Map.of(
			"title", copy.subject(),
			"logoLightId", LOGO_LIGHT_ID,
			"logoDarkId", LOGO_DARK_ID,
			"heading", copy.heading().replace("{name}", HtmlUtils.htmlEscape(event.firstName())),
			"intro", copy.intro(),
			"code", HtmlUtils.htmlEscape(event.code()),
			"minutes", String.valueOf(validMinutes),
			"steps", renderSteps(copy.steps()));
		return new EmailMessage(event.email(), copy.subject(), fill(values), logos);
	}

	// Una sola pasada: un valor que contenga "{{algo}}" no se vuelve a interpretar como marcador.
	private String fill(Map<String, String> values) {
		return PLACEHOLDER.matcher(template)
			.replaceAll(match -> Matcher.quoteReplacement(values.getOrDefault(match.group(1), "")));
	}

	private String renderSteps(List<String> steps) {
		if (steps.isEmpty()) {
			return "";
		}
		String items = steps.stream()
			.map(step -> "<li style=\"margin-bottom:6px;\">" + step + "</li>")
			.collect(Collectors.joining());
		return STEPS_TEMPLATE.replace("{{items}}", items);
	}

	private Copy copyFor(OneTimeCodePurpose purpose) {
		return switch (purpose) {
			case CREATION -> new Copy(
				"Creación de usuario en Promaty",
				"¡Bienvenid@ {name}!",
				"Se creó tu usuario en Promaty. Para ingresar por primera vez, usa tu email y esta clave temporal:",
				List.of(
					"Ingresa a Promaty con tu email y esta clave temporal.",
					"Te pediremos crear una contraseña nueva y personal."));
			case RECOVERY -> new Copy(
				"Recuperación de contraseña en Promaty",
				"Hola {name}",
				"Recibimos una solicitud para recuperar tu contraseña. Usa este código para crear una nueva:",
				List.of());
		};
	}

	private static String loadTemplate() {
		try {
			return StreamUtils.copyToString(new ClassPathResource(TEMPLATE_PATH).getInputStream(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException("No se pudo cargar la plantilla de correo " + TEMPLATE_PATH, e);
		}
	}
}
