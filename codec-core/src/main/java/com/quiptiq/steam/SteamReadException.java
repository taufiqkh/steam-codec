package com.quiptiq.steam;

/**
 * For errors that occur during the formation of a key/value mapping
 */
public class SteamReadException extends Exception {
    public SteamReadException(String message) {
        super(message);
    }
}
