package com.xadrez.chess_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.xadrez.chess_backend.record.CorsProperties;
import com.xadrez.chess_backend.record.JwtProperties;
import com.xadrez.chess_backend.record.WebSocketProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({
	JwtProperties.class,
	CorsProperties.class,
	WebSocketProperties.class
})
public class ChessBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChessBackendApplication.class, args);
	}

}
