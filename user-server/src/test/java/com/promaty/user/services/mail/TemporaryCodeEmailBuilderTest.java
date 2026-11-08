package com.promaty.user.services.mail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.promaty.user.entity.OneTimeCodePurpose;
import com.promaty.user.services.credential.TemporaryCodeIssuedEvent;

class TemporaryCodeEmailBuilderTest {

	private static final long VALID_MINUTES = 30;

	private final TemporaryCodeEmailBuilder builder = new TemporaryCodeEmailBuilder(VALID_MINUTES);

	@Test
	void build_paraCreacion_armaElCorreoDeBienvenidaConElCodigoYLaVigencia() {
		TemporaryCodeIssuedEvent event =
			new TemporaryCodeIssuedEvent("ana@promaty.com", "Ana", "ABCD2345", OneTimeCodePurpose.CREATION);

		EmailMessage message = builder.build(event);

		assertThat(message.to()).isEqualTo("ana@promaty.com");
		assertThat(message.subject()).isEqualTo("Creación de usuario en Promaty");
		assertThat(message.htmlBody())
			.contains("¡Bienvenid@ Ana!")
			.contains("ABCD2345")
			.contains("30 minutos");
	}

	@Test
	void build_paraRecuperacion_usaElAsuntoYElTextoDeRecuperacion() {
		TemporaryCodeIssuedEvent event =
			new TemporaryCodeIssuedEvent("ana@promaty.com", "Ana", "ABCD2345", OneTimeCodePurpose.RECOVERY);

		EmailMessage message = builder.build(event);

		assertThat(message.subject()).isEqualTo("Recuperación de contraseña en Promaty");
		assertThat(message.htmlBody())
			.contains("Hola Ana")
			.contains("recuperar tu contraseña")
			.contains("ABCD2345");
	}

	@Test
	void build_paraCreacion_incluyeLosPasosParaContinuar() {
		TemporaryCodeIssuedEvent event =
			new TemporaryCodeIssuedEvent("ana@promaty.com", "Ana", "ABCD2345", OneTimeCodePurpose.CREATION);

		EmailMessage message = builder.build(event);

		assertThat(message.htmlBody())
			.contains("Cómo continuar")
			.contains("Te pediremos crear una contraseña nueva");
	}

	@Test
	void build_paraRecuperacion_noIncluyeLosPasosDeBienvenida() {
		TemporaryCodeIssuedEvent event =
			new TemporaryCodeIssuedEvent("ana@promaty.com", "Ana", "ABCD2345", OneTimeCodePurpose.RECOVERY);

		EmailMessage message = builder.build(event);

		assertThat(message.htmlBody()).doesNotContain("Cómo continuar");
	}

	@Test
	void build_incrustaLosDosLogosYElCorreoLosReferenciaPorContentId() {
		TemporaryCodeIssuedEvent event =
			new TemporaryCodeIssuedEvent("ana@promaty.com", "Ana", "ABCD2345", OneTimeCodePurpose.CREATION);

		EmailMessage message = builder.build(event);

		assertThat(message.inlineImages()).hasSize(2).allSatisfy(image -> {
			assertThat(image.resource().exists()).isTrue();
			assertThat(image.contentType()).isEqualTo("image/png");
			assertThat(message.htmlBody()).contains("cid:" + image.contentId());
		});
	}

	@Test
	void build_declaraSoporteDeModoOscuroYNoIncluyeEnlaces() {
		TemporaryCodeIssuedEvent event =
			new TemporaryCodeIssuedEvent("ana@promaty.com", "Ana", "ABCD2345", OneTimeCodePurpose.CREATION);

		EmailMessage message = builder.build(event);

		assertThat(message.htmlBody())
			.contains("prefers-color-scheme: dark")
			.doesNotContain("href=")
			.doesNotContain("{{");
	}

	@Test
	void build_unNombreConMarcadoresNoSeVuelveAInterpretar() {
		TemporaryCodeIssuedEvent event =
			new TemporaryCodeIssuedEvent("ana@promaty.com", "{{code}}", "ABCD2345", OneTimeCodePurpose.RECOVERY);

		EmailMessage message = builder.build(event);

		assertThat(message.htmlBody()).contains("Hola {{code}}");
	}

	@Test
	void build_escapaElHtmlDelNombreParaQueNoSePuedaInyectarContenido() {
		TemporaryCodeIssuedEvent event = new TemporaryCodeIssuedEvent(
			"ana@promaty.com", "<script>alert(1)</script>", "ABCD2345", OneTimeCodePurpose.CREATION);

		EmailMessage message = builder.build(event);

		assertThat(message.htmlBody())
			.doesNotContain("<script>")
			.contains("&lt;script&gt;");
	}
}
