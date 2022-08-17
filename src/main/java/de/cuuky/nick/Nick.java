package de.cuuky.nick;

import com.bringholm.nametagchanger.NameTagChanger;
import com.bringholm.nametagchanger.skin.Skin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Nick extends JavaPlugin implements Listener {

    private final NameTagChanger nameTagChanger = NameTagChanger.INSTANCE;
    private boolean nameTagEnabled;
    private boolean changeTab;
    private String nameTagName;

    private boolean skinEnabled;
    private String changeSkinName;
    private Skin changeSkin;

    private void process(Player player) {
        if (this.nameTagEnabled) this.changeNameTag(player);
        if (this.skinEnabled) this.changeSkin(player);
    }

    private void changeNameTag(Player player) {
        String name = this.nameTagName.replace("&", "§");
        this.nameTagChanger.changePlayerName(player, name);
        if (this.changeTab) player.setPlayerListName(name);
    }

    private void changeSkin(Player player) {
        if (this.changeSkin == null) return;
        this.nameTagChanger.setPlayerSkin(player, this.changeSkin);
    }

    private void loadConfiguration() {
        File file = new File(this.getDataFolder(), "config.yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        configuration.options().copyDefaults(true);

        configuration.addDefault("nametag.enabled", true);
        configuration.addDefault("nametag.name", "&7Nick");
        configuration.addDefault("nametag.tablist", false);
        configuration.addDefault("skin.enabled", true);
        configuration.addDefault("skin.playerSkinName", "Cuuky");

        this.nameTagEnabled = configuration.getBoolean("nametag.enabled");
        this.nameTagName = configuration.getString("nametag.name");
        this.changeTab = configuration.getBoolean("nametag.tablist");
        this.skinEnabled = configuration.getBoolean("skin.enabled");
        this.changeSkinName = configuration.getString("skin.playerSkinName");

        try {
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadSkin() {
        this.nameTagChanger.getSkin(this.changeSkinName, (skin, b, e) -> {
            if (b) this.changeSkin = skin;
            else throw new RuntimeException(e);
        });
    }

    @Override
    public void onEnable() {
        System.out.println("Enabling " + this.getDescription().getName() + " v" +
                this.getDescription().getVersion() + " by " + this.getDescription().getAuthors().get(0));
        this.loadConfiguration();
        if (!this.nameTagChanger.isEnabled()) {
            this.nameTagChanger.setPlugin(this);
            this.nameTagChanger.enable();
        }

        if (this.skinEnabled) this.loadSkin();

        this.getServer().getOnlinePlayers().forEach(this::process);
        this.getServer().getPluginManager().registerEvents(this, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (this.nameTagChanger.isEnabled()) this.nameTagChanger.disable();
        super.onDisable();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.process(event.getPlayer());
    }
}
