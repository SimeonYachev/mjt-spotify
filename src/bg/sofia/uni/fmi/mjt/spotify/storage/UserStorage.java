package bg.sofia.uni.fmi.mjt.spotify.storage;

import java.util.Map;

public record UserStorage(Map<String, String> registeredUsers,
                          Map<Integer, String> loggedUsers) {}
