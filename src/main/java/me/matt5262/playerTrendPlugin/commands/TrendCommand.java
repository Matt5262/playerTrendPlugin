package me.matt5262.playerTrendPlugin.commands;

import me.matt5262.playerTrendPlugin.PlayerTrendPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TrendCommand implements CommandExecutor {

    private final PlayerTrendPlugin plugin;

    public TrendCommand(PlayerTrendPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        Connection conn = plugin.getConnection();
        if (conn == null) {
            sender.sendMessage("§c⚠ Database not connected.");
            return true;
        }

        String sql = """
            WITH last_week AS (
                SELECT date, COUNT(DISTINCT uuid) AS players
                FROM daily_logins
                WHERE date >= date('now', '-7 day')
                GROUP BY date;
            ),
            prev_week AS (
                SELECT date, COUNT(DISTINCT uuid) AS players
                FROM daily_logins
                WHERE date >= date('now', '-14 day') AND date < date('now', '-7 day')
                GROUP BY date
            )
            SELECT
                (SELECT AVG(players) FROM last_week) AS avg_last,
                (SELECT AVG(players) FROM prev_week) AS avg_prev;
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                double avgLast = rs.getDouble("avg_last");
                double avgPrev = rs.getDouble("avg_prev");

                if (rs.wasNull()) {
                    sender.sendMessage("§eNot enough data to calculate trends yet.");
                    return true;
                }

                double diff = avgLast - avgPrev;
                String trendSymbol = diff > 0 ? "§a📈" : (diff < 0 ? "§c📉" : "§e➖");

                sender.sendMessage("§6=== Player Trend Report ===");
                sender.sendMessage("§7Last 7 days avg: §e" + String.format("%.2f", avgLast));
                sender.sendMessage("§7Prev 7 days avg: §e" + String.format("%.2f", avgPrev));
                sender.sendMessage(trendSymbol + " §7Trend: " +
                        (diff > 0 ? "§aUp §7(+" + String.format("%.2f", diff) + ")" :
                                diff < 0 ? "§cDown §7(" + String.format("%.2f", diff) + ")" :
                                        "§eStable"));
            } else {
                sender.sendMessage("§eNo data yet.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            sender.sendMessage("§cError fetching data.");
        }

        return true;
    }
}
