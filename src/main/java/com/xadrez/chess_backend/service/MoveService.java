package com.xadrez.chess_backend.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.xadrez.chess_backend.dto.move.MakeMoveRequest;
import com.xadrez.chess_backend.dto.move.MoveResponse;
import com.xadrez.chess_backend.repository.MoveRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MoveService {

    private final MoveRepository moveRepository;

    public MoveResponse makeMove(MakeMoveRequest request, UUID gameId) {
        return null;
    }
}
