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
        // does not auto close because it is a normal try, not a try-with-resources
        try {
            // if not data folder exists (/playerTrendPlugin) and try to create the folder but if it does not then throw exception.
            if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
                throw new SQLException("Failed to create plugin data directory: " + getDataFolder());
            }
            // creates or opens the connection to (/playerdata.db) basically you set connection to a path. Since connection has the Connection type, it can do connection stuff now.
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
                // tripe quotes is for multi lines and for visuals
                // id INTEGER PRIMARY KEY AUTOINCREMENT means it makes a colum named id and is autoincrement with 1, 2, 3 etc.
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
