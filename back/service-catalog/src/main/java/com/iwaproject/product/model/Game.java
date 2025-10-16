package com.iwaproject.product.model;

public enum Game {
    LEAGUE_OF_LEGENDS("League of Legends"),
    TEAMFIGHT_TACTICS("Teamfight Tactics"),
    ROCKET_LEAGUE("Rocket League"),
    VALORANT("Valorant"),
    OTHER("Other");

    private final String displayName;

    Game(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

