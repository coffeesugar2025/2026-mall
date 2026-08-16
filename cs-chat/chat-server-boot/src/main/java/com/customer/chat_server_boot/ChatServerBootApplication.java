package com.customer.chat_server_boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
@MapperScan("com.customer.chat_server_boot.mapper")
public class ChatServerBootApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChatServerBootApplication.class, args);
	}

}
