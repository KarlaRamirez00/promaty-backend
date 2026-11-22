package com.promaty.rrhh.services.rexplus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.promaty.rrhh.config.RestClientConfig;
import com.promaty.rrhh.config.RexPlusHttpConfig;

class RexPlusClientSelectionTest {

	private final ApplicationContextRunner runner = new ApplicationContextRunner()
		.withUserConfiguration(RestClientConfig.class, RexPlusHttpConfig.class, RexPlusStubClient.class, RexPlusHttpClient.class);

	@Test
	void sinPropiedad_usaElStubPorDefecto() {
		runner.run(context -> {
			assertThat(context).hasSingleBean(RexPlusClient.class);
			assertThat(context).hasSingleBean(RexPlusStubClient.class);
			assertThat(context).doesNotHaveBean(RexPlusHttpClient.class);
		});
	}

	@Test
	void modoStub_usaElStub() {
		runner.withPropertyValues("rexplus.mode=stub").run(context -> {
			assertThat(context).hasSingleBean(RexPlusStubClient.class);
			assertThat(context).doesNotHaveBean(RexPlusHttpClient.class);
		});
	}

	@Test
	void modoHttp_usaElAdaptadorHttp() {
		runner.withPropertyValues(
			"rexplus.mode=http",
			"rexplus.http.base-url=http://localhost:8090",
			"rexplus.http.api-key=dev-key"
		).run(context -> {
			assertThat(context).hasSingleBean(RexPlusClient.class);
			assertThat(context).hasSingleBean(RexPlusHttpClient.class);
			assertThat(context).doesNotHaveBean(RexPlusStubClient.class);
		});
	}

	@Test
	void modoHttpSinApiKey_elContextoNoArranca() {
		runner.withPropertyValues(
			"rexplus.mode=http",
			"rexplus.http.base-url=http://localhost:8090",
			"rexplus.http.api-key="
		).run(context -> assertThat(context).hasFailed());
	}
}
