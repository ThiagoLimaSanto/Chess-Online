package com.xadrez.chess_backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateUserRequest {

    @NotBlank(message = "Nome do usuário não pode ser vazio")
    @Size(min = 3, message = "Nome do usuário deve ter pelo menos 3 caracteres")
    private String username;

    @NotBlank(message = "Senha não pode ser vazia")
    @Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres")
    private String password;
}
