package com.xadrez.chess_backend.command;

import org.springframework.stereotype.Component;

@Component
public class GameCommandExecutor {

    public <R> R execute(GameCommand<R> command) {
        return command.execute();
    }
}
