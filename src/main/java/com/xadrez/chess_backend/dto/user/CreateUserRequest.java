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
    @Size(min = 3, message = "nome do usuário deve ter mais de 3 caracteres")
    private String username;
}