package me.matt5262.playerTrendPlugin;

import me.matt5262.playerTrendPlugin.commands.TrendCommand;
import me.matt5262.playerTrendPlugin.listeners.JoinListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class PlayerTrendPlugin extends JavaPlugin {

    private Connection connection;

    @Override
    public void onEnable() {
        getLogger().info("PlayerTrendPlugin enabled!");
        setupDatabase();

        // Register listener
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        // Register command
        getCommand("trend").setExecutor(new TrendCommand(this));
    }

    @Override
    public void onDisable() {
        if (connection != null) {
            try {
                connection.close();
                getLogger().info("Database connection closed.");
            } catch (SQLException e) {
                getLogger().severe("Failed to close database connection!");
                e.printStackTrace();
            }
        }
    }

    private void setupDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + getDataFolder() + "/playerdata.db");

            try (Statement stmt = connection.createStatement()) {
                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS daily_logins (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        uuid TEXT NOT NULL,
                        date TEXT NOT NULL,
                        UNIQUE(uuid, date)
                    )
                """);
            }

            getLogger().info("Database initialized.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
