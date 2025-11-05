package com.iwaproject.product.model;

public enum Game {
    LEAGUE_OF_LEGENDS("League_of_Legends"),
    TEAMFIGHT_TACTICS("Teamfight_Tactics"),
    ROCKET_LEAGUE("Rocket_League"),
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

