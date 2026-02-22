package fr.academy.cdp.domain.model;

import net.minecraft.util.math.BlockPos;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DungeonSession {
    private final List<UUID> players = new ArrayList<>();
    private final PortalSettings settings;
    private final BlockPos portalPos; // Position du bloc physique
    private boolean active = false;

    public DungeonSession(PortalSettings settings, BlockPos portalPos) {
        this.settings = settings;
        this.portalPos = portalPos;
    }

    public void setActive(boolean active) { this.active = active; }
    public boolean isActive() { return active; }
    public List<UUID> getPlayers() { return players; }
    public BlockPos getPortalPos() { return portalPos; }
    public PortalSettings getSettings() { return settings; }

    public boolean addPlayer(UUID uuid) {
        if (active || players.size() >= 4) return false;
        if (!players.contains(uuid)) players.add(uuid);
        return true;
    }

    public UUID getLeader() {
        return players.isEmpty() ? null : players.get(0);
    }

    private int gridX;

    public void setGridX(int x) { this.gridX = x; }
    public int getGridX() { return gridX; }
}