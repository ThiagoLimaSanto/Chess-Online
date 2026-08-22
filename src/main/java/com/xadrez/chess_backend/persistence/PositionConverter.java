package com.xadrez.chess_backend.persistence;

import com.xadrez.chess_backend.record.Position;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class PositionConverter implements AttributeConverter<Position, String> {

    @Override
    public String convertToDatabaseColumn(Position position) {
        return position == null ? null : position.row() + "," + position.column();
    }

    @Override
    public Position convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }

        String[] coordinates = value.split(",", -1);
        if (coordinates.length != 2) {
            throw new IllegalArgumentException("Posição persistida inválida: " + value);
        }

        return new Position(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));
    }
}
