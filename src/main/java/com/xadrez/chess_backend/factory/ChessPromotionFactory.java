package com.xadrez.chess_backend.factory;

import org.springframework.stereotype.Component;

import com.xadrez.chess_backend.enums.PieceColor;
import com.xadrez.chess_backend.enums.PieceType;
import com.xadrez.chess_backend.exception.PieceInvalidPromotion;
import com.xadrez.chess_backend.model.chess.Bishop;
import com.xadrez.chess_backend.model.chess.Knight;
import com.xadrez.chess_backend.model.chess.Pawn;
import com.xadrez.chess_backend.model.chess.Piece;
import com.xadrez.chess_backend.model.chess.Queen;
import com.xadrez.chess_backend.model.chess.Rook;

@Component
public class ChessPromotionFactory implements PromotionFactory {

    @Override
    public Piece create(PieceType type, PieceColor color) {
        return switch (type) {
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color);
            case KNIGHT -> new Knight(color);
            case BISHOP -> new Bishop(color);
            case PAWN -> new Pawn(color);
            default -> throw new PieceInvalidPromotion("Peça inválida para promoção");
        };
    };

}
