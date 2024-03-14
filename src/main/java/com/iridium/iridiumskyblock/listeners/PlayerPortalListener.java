package com.iridium.iridiumskyblock.listeners;

import com.iridium.iridiumcore.dependencies.xseries.XMaterial;
import com.iridium.iridiumcore.utils.StringUtils;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Objects;

public class PlayerPortalListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTouchPortal(final PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location location = player.getLocation();
        Material blockType = location.getBlock().getType();
        if (blockType != Material.NETHER_PORTAL && blockType != Material.END_PORTAL_FRAME) {
            return;
        }
        IridiumSkyblock.getInstance().getTeamManager().getTeamViaLocation(event.getFrom()).ifPresent(island -> {
            if (blockType == Material.NETHER_PORTAL) {
                if (island.getLevel() < IridiumSkyblock.getInstance().getConfiguration().netherUnlockLevel) {
                    event.getPlayer().sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().netherLocked
                            .replace("%level%", String.valueOf(IridiumSkyblock.getInstance().getConfiguration().netherUnlockLevel))
                            .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)
                    ));
                    return;
                }
                World nether = IridiumSkyblock.getInstance().getIslandManager().getWorld(World.Environment.NETHER);
                if (nether == null) {
                    event.getPlayer().sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().netherIslandsDisabled
                            .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)
                    ));
                    return;
                }
                World world = Objects.equals(event.getFrom().getWorld(), nether) ? IridiumSkyblock.getInstance().getTeamManager().getWorld(World.Environment.NORMAL) : nether;
                LocationUtils.tpToSafeLocation(event.getPlayer(), island.getCenter(world), island);
            } else {
                if (island.getLevel() < IridiumSkyblock.getInstance().getConfiguration().endUnlockLevel) {
                    event.getPlayer().sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().endLocked
                            .replace("%level%", String.valueOf(IridiumSkyblock.getInstance().getConfiguration().endUnlockLevel))
                            .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)
                    ));
                    return;
                }
                World end = IridiumSkyblock.getInstance().getIslandManager().getWorld(World.Environment.THE_END);
                if (end == null) {
                    event.getPlayer().sendMessage(StringUtils.color(IridiumSkyblock.getInstance().getMessages().endIslandsDisabled
                            .replace("%prefix%", IridiumSkyblock.getInstance().getConfiguration().prefix)
                    ));
                    return;
                }
                World world = Objects.equals(event.getFrom().getWorld(), end) ? IridiumSkyblock.getInstance().getTeamManager().getWorld(World.Environment.NORMAL) : end;
                if(XMaterial.supports(15)) {
                    return;
                }
                LocationUtils.tpToSafeLocation(event.getPlayer(), island.getCenter(world), island);
            }
        });
    }
}
