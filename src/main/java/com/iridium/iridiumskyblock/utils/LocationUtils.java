package com.iridium.iridiumskyblock.utils;

import com.iridium.iridiumcore.dependencies.xseries.XMaterial;
import com.iridium.iridiumcore.multiversion.MultiVersion;
import com.iridium.iridiumskyblock.IridiumSkyblock;
import com.iridium.iridiumskyblock.database.Island;
import org.apache.logging.log4j.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocationUtils {

    private static final List<Material> unsafeBlocks = Stream.of(
            XMaterial.END_PORTAL,
            XMaterial.WATER,
            XMaterial.LAVA
    ).map(XMaterial::parseMaterial).collect(Collectors.toList());

    public static boolean isSafe(Location location) {
        if (IridiumSkyblock.getInstance().isTesting()) return true;
        Block block = location.getBlock();
        Block above = location.clone().add(0, 1, 0).getBlock();
        Block below = location.clone().subtract(0, 1, 0).getBlock();
        MultiVersion multiVersion = IridiumSkyblock.getInstance().getMultiVersion();
        return multiVersion.isPassable(block) && multiVersion.isPassable(above) && !multiVersion.isPassable(below) && !unsafeBlocks.contains(below.getType()) && !unsafeBlocks.contains(block.getType()) && !unsafeBlocks.contains(above.getType());
    }

    /**
     * Gets a safe location on the island
     *
     * @param location The location we want to teleport
     * @param island   The island we are inside
     * @return A safe Location, if none found return null
     */
    @Nullable
    public static CompletableFuture<Location> getSafeLocation(final Location location, Island island){
        return CompletableFuture.supplyAsync(()->{
            try {
                final Location[] resLocation = {location};
                Bukkit.getRegionScheduler().run(IridiumSkyblock.getInstance(), resLocation[0],(task)->{
                    World world = location.getWorld();
                    if (world == null) resLocation[0] = location;
                    if (island == null) resLocation[0] =  location;
                    if (isSafe(location)) resLocation[0] =  location;
                    Location highest = getHighestLocation(location.getBlockX(), location.getBlockZ(), world);
                    if (isSafe(highest)) resLocation[0] =  highest;

                    Location pos1 = island.getPosition1(world);
                    Location pos2 = island.getPosition2(world);
                    for (int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++) {
                        for (int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++) {
                            Location newLocation = getHighestLocation(x, z, world);
                            if (isSafe(newLocation)) resLocation[0] =  newLocation;
                        }
                    }
                });
                return resLocation[0];
            } catch (Exception e){
                IridiumSkyblock.getInstance().getLogger().warning(e.toString());
                return null;
            }
        });
    }
    // public static CompletableFuture<Location> getSafeLocation(Location location, Island island) {
    //     CompletableFuture<Location> completableFuture = new CompletableFuture<>();
    //     try {
    //         World world = location.getWorld();
    //         if (world == null) completableFuture.complete(location);
    //         if (island == null) completableFuture.complete(location);
    //         if (isSafe(location)) completableFuture.complete(location);
    //         Location highest = getHighestLocation(location.getBlockX(), location.getBlockZ(), world);
    //         if (isSafe(highest)) completableFuture.complete(highest);
    //
    //         Location pos1 = island.getPosition1(world);
    //         Location pos2 = island.getPosition2(world);
    //         for (int x = pos1.getBlockX(); x <= pos2.getBlockX(); x++) {
    //             for (int z = pos1.getBlockZ(); z <= pos2.getBlockZ(); z++) {
    //                 Location newLocation = getHighestLocation(x, z, world);
    //                 if (isSafe(newLocation)) completableFuture.complete(newLocation);
    //             }
    //         }
    //         completableFuture.complete(null);
    //     } catch (Exception e){
    //         IridiumSkyblock.getInstance().getLogger().warning(e.toString());
    //         completableFuture.complete(null);
    //     }
    //     return completableFuture;
    // }

    /**
     * Gets the highest Location in a world
     * Mojang was dum and changed how this worked
     *
     * @param x     the x coord
     * @param z     the z coord
     * @param world The world
     * @return The highest AIR location
     */
    private static Location getHighestLocation(int x, int z, World world) {
        Block block = world.getHighestBlockAt(x, z);
        while (!IridiumSkyblock.getInstance().getMultiVersion().isPassable(block)) {
            block = block.getLocation().add(0, 1, 0).getBlock();
        }
        return block.getLocation().add(0.5, 0, 0.5);
    }

    /**
     * With the data pack, you can modify the height limits and in the Spigot API.
     * It exists since 1.17 on Spigot and 1.16 at PaperMC.
     *
     * @param world The world
     * @return The lowest AIR location.
     */
    public static int getMinHeight(World world) {
        return XMaterial.getVersion() >= 17 ? world.getMinHeight() : 0;  // World#getMinHeight() -> Available only in 1.17 Spigot and 1.16.5 PaperMC
    }
}
