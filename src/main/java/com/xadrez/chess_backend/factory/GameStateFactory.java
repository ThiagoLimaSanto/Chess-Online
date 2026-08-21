package com.xadrez.chess_backend.factory;

import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.state.game.FinishedState;
import com.xadrez.chess_backend.state.game.GameState;
import com.xadrez.chess_backend.state.game.InProgressState;
import com.xadrez.chess_backend.state.game.WaitingState;
import com.xadrez.chess_backend.enums.GameStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GameStateFactory {
    private final WaitingState waitingState;
    private final InProgressState inProgressState;
    private final FinishedState finishedState;

    public GameState waiting() {
        return waitingState;
    }

    public GameState inProgress() {
        return inProgressState;
    }

    public GameState finished() {
        return finishedState;
    }

    public GameState forStatus(GameStatus status) {
        return switch (status) {
            case WAITING -> waiting();
            case IN_PROGRESS -> inProgress();
            case FINISHED -> finished();
        };
    }
}
