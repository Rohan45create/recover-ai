package com.recoverai;

import org.springframework.boot.SpringApplication;

public class TestRecoveraiApplication {

	public static void main(String[] args) {
		SpringApplication.from(RecoveraiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
