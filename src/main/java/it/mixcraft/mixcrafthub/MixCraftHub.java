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

    public void onEnable() {
        // Messaggio di avvio
        getLogger().log(Level.INFO, "MixHUB Plugin started!");
        // Caria o crea configurazione
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
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Registra il comando /hub
        getProxy().getPluginManager().registerCommand(this, new HubCommand());

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

                if (lobbyServer != null) {
                    player.connect(getProxy().getServerInfo(lobbyServer));
                    if (feedbackMessage != null) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', feedbackMessage));
                    }
                } else  {
                    player.sendMessage("Configurazione non valida!");
                }

            }
        }
    }
}