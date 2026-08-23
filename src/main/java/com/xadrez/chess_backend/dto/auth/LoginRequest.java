package com.xadrez.chess_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Nome do usuário não pode ser vazio")
    private String username;

    @NotBlank(message = "Senha não pode ser vazia")
    private String password;
}