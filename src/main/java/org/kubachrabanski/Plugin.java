package org.kubachrabanski;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.kubachrabanski.listeners.PlayerListener;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;

public final class Plugin extends JavaPlugin {

    private static void purgeFiles(File parent) {
        if (parent.isDirectory()) {
            File[] files = parent.listFiles();

            if (files != null) {
                for (File child : files) {
                    purgeFiles(child);
                }
            }
        }

        parent.deleteOnExit();
    }

    @Override
    public void onDisable() {
        Path worldPath = Paths.get("world");

        getConfig().getStringList("purge_files").stream()
                .map(worldPath::resolve)
                .forEach(path -> purgeFiles(path.toFile()));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginManager manager = Bukkit.getPluginManager();
        manager.registerEvents(new PlayerListener(), this);

        try {
            final double x = getConfig().getDouble("spawn_point.x");
            final double y = getConfig().getDouble("spawn_point.y");
            final double z = getConfig().getDouble("spawn_point.z");

            World world = Bukkit.getWorld("world");

            getLogger().info(
                    format("Spawn location at: %.2f, %.2f, %.2f, %b", x, y, z,
                            world.setSpawnLocation(new Location(world, x, y, z)))
            );

            world.setKeepSpawnInMemory(true);

            world.setTime(6000);
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);

            world.setAutoSave(false);
        }
        catch (NullPointerException ignore) {
            getLogger().severe(
                    "Failed to enable the plugin with current configuration"
            );
            Bukkit.shutdown();
        }
    }
}
