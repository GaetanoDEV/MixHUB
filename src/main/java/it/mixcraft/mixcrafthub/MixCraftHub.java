package it.mixcraft.mixcrafthub;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.logging.Level;

public class MixCraftHub extends Plugin {
    private Configuration configuration;
    private String consoleOnlyMessage;
    private String configReloadedMessage;
    private int connectDelaySeconds;

    public void onEnable() {
        // Messaggio di avvio
        getLogger().log(Level.INFO, "MixHUB Plugin started succesfully!");
        // Carica o crea configurazione
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try (InputStream inputStream = getResourceAsStream("config.yml")) {
                Files.copy(inputStream, configFile.toPath());
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        // Carica la configurazione
        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
            consoleOnlyMessage = ChatColor.translateAlternateColorCodes('&', configuration.getString("messages.console-only"));
            configReloadedMessage = ChatColor.translateAlternateColorCodes('&', configuration.getString("messages.config-reloaded"));
            connectDelaySeconds = configuration.getInt("connect-delay-seconds", 0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Registra i comandi
        getProxy().getPluginManager().registerCommand(this, new HubCommand());
        getProxy().getPluginManager().registerCommand(this, new ReloadCommand());
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "MixHUB - Plugin disabled!");
    }

    private class HubCommand extends Command {
        public HubCommand() {
            super("hub");
        }

        @Override
        public void execute(CommandSender sender, String[] args){
            if (sender instanceof ProxiedPlayer) {
                ProxiedPlayer player = (ProxiedPlayer) sender;


                // Ottiene il server della lobby da config
                String lobbyServer = configuration.getString("lobby-server");
                String feedbackMessage = configuration.getString("feedback-message");
                String delayMessage = configuration.getString("delay-message");


                if (lobbyServer != null) {
                    if (connectDelaySeconds > 0) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', delayMessage));
                        getProxy().getScheduler().schedule(MixCraftHub.this, () -> {
                            player.connect(getProxy().getServerInfo(lobbyServer));
                            if (feedbackMessage != null) {
                                player.sendMessage(ChatColor.translateAlternateColorCodes('&', feedbackMessage));
                            }
                        }, connectDelaySeconds, java.util.concurrent.TimeUnit.SECONDS);
                    } else {
                        player.connect(getProxy().getServerInfo(lobbyServer));
                        if (feedbackMessage != null) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', feedbackMessage));
                        }
                    }
                } else {
                    player.sendMessage("Configurazione non valida!");
                }
            }
        }
    }

    private class ReloadCommand extends Command {
        public ReloadCommand() {
            super("mixhubreload");
        }

        @Override
        public void execute(CommandSender sender, String[] args) {
            if (sender instanceof ProxiedPlayer) {
                sender.sendMessage(consoleOnlyMessage);
                return;
            }

            reloadConfiguration();
            sender.sendMessage(configReloadedMessage);
        }
    }

    private void reloadConfiguration() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            try {
                configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
                consoleOnlyMessage = ChatColor.translateAlternateColorCodes('&', configuration.getString("messages.console-only"));
                configReloadedMessage = ChatColor.translateAlternateColorCodes('&', configuration.getString("messages.config-reloaded"));
                connectDelaySeconds = configuration.getInt("connect-delay-seconds", 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
