package com.promaty.user.services.user.business.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.promaty.user.dto.user.CreateUserDto;
import com.promaty.user.entity.Role;
import com.promaty.user.entity.User;
import com.promaty.user.repository.RoleRepository;

@ExtendWith(MockitoExtension.class)
class CreateUserBuilderTest {

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private CreateUserBuilder builder;

	@Test
	void build_guardaElHashDelCodigoTemporalYExigeCambiarLaClave() {
		Role role = new Role();
		when(roleRepository.getReferenceById(2L)).thenReturn(role);
		when(passwordEncoder.encode("ABCD2345")).thenReturn("$2a$hash");
		CreateUserDto dto = new CreateUserDto();
		dto.setFirstName("Ana");
		dto.setLastName("Perez");
		dto.setEmail("ana.perez@promaty.cl");
		dto.setRoleId(2L);

		User user = builder.build(dto, "ABCD2345");

		assertThat(user.getPassword()).isEqualTo("$2a$hash");
		assertThat(user.getMustChangePassword()).isTrue();
		assertThat(user.getActive()).isTrue();
		assertThat(user.getEmail()).isEqualTo("ana.perez@promaty.cl");
		assertThat(user.getRole()).isSameAs(role);
	}
}
