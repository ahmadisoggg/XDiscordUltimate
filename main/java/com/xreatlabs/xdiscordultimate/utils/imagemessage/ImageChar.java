package com.xreatlabs.xdiscordultimate.utils.imagemessage;

public enum ImageChar {
    BLOCK('█'),
    DARK_SHADE('▓'),
    MEDIUM_SHADE('▒'),
    LIGHT_SHADE('░');

    private final char character;

    ImageChar(char character) {
        this.character = character;
    }

    public char getChar() {
        return this.character;
    }
}
