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
}
