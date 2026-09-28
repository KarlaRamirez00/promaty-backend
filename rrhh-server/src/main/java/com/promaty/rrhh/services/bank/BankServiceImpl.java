package com.promaty.rrhh.services.bank;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.promaty.rrhh.dto.bank.BankOptionDto;
import com.promaty.rrhh.repository.BankRepository;

@Service
public class BankServiceImpl implements BankService {

	private static final String CODIGO_BANCO_ESTADO = "BANCO_ESTADO";

	private final BankRepository bankRepository;

	public BankServiceImpl(BankRepository bankRepository) {
		this.bankRepository = bankRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<BankOptionDto> listOptions() {
		return bankRepository.findByActiveTrueOrderByName()
			.stream()
			.map(bank -> new BankOptionDto(
				bank.getId(),
				bank.getName(),
				bank.getCode(),
				CODIGO_BANCO_ESTADO.equals(bank.getCode())
			))
			.toList();
	}
}
