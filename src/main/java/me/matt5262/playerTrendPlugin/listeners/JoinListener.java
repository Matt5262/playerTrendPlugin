package me.matt5262.playerTrendPlugin.listeners;

import me.matt5262.playerTrendPlugin.PlayerTrendPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class JoinListener implements Listener {

    private final PlayerTrendPlugin plugin;

    public JoinListener(PlayerTrendPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Connection conn = plugin.getConnection();
        if (conn == null) return;

        String uuid = event.getPlayer().getUniqueId().toString();
        String today = LocalDate.now().toString();

        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT OR IGNORE INTO daily_logins (uuid, date) VALUES (?, ?)"
        )) {
            ps.setString(1, uuid);
            ps.setString(2, today);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
