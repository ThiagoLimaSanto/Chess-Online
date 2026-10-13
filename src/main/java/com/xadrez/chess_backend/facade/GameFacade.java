package com.xadrez.chess_backend.facade;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.command.AbandonGameCommand;
import com.xadrez.chess_backend.command.GameCommandExecutor;
import com.xadrez.chess_backend.command.MakeMoveCommand;
import com.xadrez.chess_backend.command.OfferDrawCommand;
import com.xadrez.chess_backend.command.RespondToDrawOfferCommand;
import com.xadrez.chess_backend.dto.game.CreateGameRequest;
import com.xadrez.chess_backend.dto.game.DrawOfferResponse;
import com.xadrez.chess_backend.dto.game.GameDetailsResponse;
import com.xadrez.chess_backend.dto.game.GameResponse;
import com.xadrez.chess_backend.dto.history.PlayerGameHistoryResponse;
import com.xadrez.chess_backend.dto.move.MakeMoveRequest;
import com.xadrez.chess_backend.dto.move.MoveHistoryResponse;
import com.xadrez.chess_backend.dto.move.MoveResponse;
import com.xadrez.chess_backend.record.PageResponse;
import com.xadrez.chess_backend.service.GameService;
import com.xadrez.chess_backend.service.MoveService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameFacade {

    private final GameService gameService;
    private final MoveService moveService;
    private final GameCommandExecutor commandExecutor;

    public GameResponse createGame(CreateGameRequest request) {
        return gameService.createGame(request);
    }

    public GameDetailsResponse getGameDetails(UUID gameId, UUID authenticatedPlayerId) {
        return gameService.getGameDetails(gameId, authenticatedPlayerId);
    }

    public PageResponse<PlayerGameHistoryResponse> getHistory(UUID userId, Pageable pageable) {
        return gameService.getHistory(userId, pageable);
    }

    public List<MoveHistoryResponse> getMoveHistory(UUID gameId, UUID authenticatedPlayerId) {
        return gameService.getMoveHistory(gameId, authenticatedPlayerId);
    }

    public List<String> getLegalMoves(UUID gameId, String fromSquare, UUID authenticatedPlayerId) {
        return moveService.getLegalMoves(gameId, fromSquare, authenticatedPlayerId);
    }

    public MoveResponse makeMove(MakeMoveRequest request, UUID gameId, UUID authenticatedPlayerId) {
        return commandExecutor.execute(new MakeMoveCommand(
                moveService, gameId, authenticatedPlayerId,
                request.getFromSquare(), request.getToSquare(), request.getPromotionType()));
    }

    public void abandonGame(UUID gameId, UUID authenticatedPlayerId) {
        commandExecutor.execute(new AbandonGameCommand(gameService, gameId, authenticatedPlayerId));
    }

    public DrawOfferResponse getDrawOffer(UUID gameId, UUID authenticatedPlayerId) {
        return gameService.getDrawOffer(gameId, authenticatedPlayerId);
    }

    public DrawOfferResponse offerDraw(UUID gameId, UUID authenticatedPlayerId) {
        return commandExecutor.execute(new OfferDrawCommand(gameService, gameId, authenticatedPlayerId));
    }

    public void respondToDrawOffer(UUID gameId, UUID authenticatedPlayerId, boolean accepted) {
        commandExecutor.execute(new RespondToDrawOfferCommand(
                gameService, gameId, authenticatedPlayerId, accepted));
    }
}
