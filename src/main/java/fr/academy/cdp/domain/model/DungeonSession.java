package fr.academy.cdp.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DungeonSession {
    private final List<UUID> players = new ArrayList<>();
    private final PortalSettings settings;

    public DungeonSession(PortalSettings settings) {
        this.settings = settings;
    }

    public boolean addPlayer(UUID uuid) {
        if (players.size() >= 4) return false;
        if (!players.contains(uuid)) players.add(uuid);
        return true;
    }

    public List<UUID> getPlayers() { return players; }
    public UUID getLeader() { return players.isEmpty() ? null : players.get(0); }
    public PortalSettings getSettings() { return settings; }
}