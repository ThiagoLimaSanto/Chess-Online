package com.xadrez.chess_backend.dto.Performance;

public class PerformanceResponse {
    private Integer gamesPlayed;
    private Integer victories;
    private Integer draws;
    private Integer losses;
    private Integer rating;

    public PerformanceResponse(Integer gamesPlayed, Integer victories, Integer draws, Integer losses, Integer rating) {
        this.gamesPlayed = gamesPlayed;
        this.victories = victories;
        this.draws = draws;
        this.losses = losses;
        this.rating = rating;
    }

    public Integer getGamesPlayed() {
        return gamesPlayed;
    }

    public Integer getVictories() {
        return victories;
    }

    public Integer getDraws() {
        return draws;
    }

    public Integer getLosses() {
        return losses;
    }

    public Integer getRating() {
        return rating;
    }
}