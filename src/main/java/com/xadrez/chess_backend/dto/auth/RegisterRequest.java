package com.xadrez.chess_backend.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Nome do usuário não pode ser vazio")
    @Size(min = 3, max = 50, message = "Nome do usuário deve ter entre 3 e 50 caracteres")
    private String username;

    @NotBlank(message = "Senha não pode ser vazia")
    @Size(min = 8, max = 100, message = "Senha deve mais de 8 caracteres")
    private String password;
}
