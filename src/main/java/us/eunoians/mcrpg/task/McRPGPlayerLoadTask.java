package us.eunoians.mcrpg.task;

import com.diamonddagger590.mccore.task.PlayerLoadTask;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;

import java.util.Optional;
import java.util.logging.Level;

/**
 * A {@link PlayerLoadTask} that loads McRPG player data
 */
//TODO javadoc
public class McRPGPlayerLoadTask extends PlayerLoadTask {

    public McRPGPlayerLoadTask(@NotNull McRPG plugin, @NotNull McRPGPlayer mcRPGPlayer) {
        super(plugin, mcRPGPlayer);
    }

    @Override
    public McRPGPlayer getCorePlayer() {
        return (McRPGPlayer) super.getCorePlayer();
    }

    @NotNull
    @Override
    public McRPG getPlugin() {
        return (McRPG) super.getPlugin();
    }

    @Override
    protected boolean loadPlayer() {
        //TODO actually load data lmfao
        getPlugin().getLogger().log(Level.INFO, "Player data loaded.");

        //Add bleed for testing
        return true;
    }

    @Override
    protected void onPlayerLoadSuccessfully() {

        //Fire event
        super.onPlayerLoadSuccessfully();

        getPlugin().getLogger().log(Level.INFO, "Player data has been loaded for player: " + getCorePlayer().getUUID());

        //Begin tracking player
        getPlugin().getPlayerManager().addPlayer(getCorePlayer());
        getPlugin().getEntityManager().trackAbilityHolder(getCorePlayer().asSkillHolder());

    }

    @Override
    protected void onPlayerLoadFail() {
        getPlugin().getLogger().log(Level.SEVERE, ChatColor.RED + "There was an issue loading in the McRPG player data for player with UUID: " + getCorePlayer().getUUID());

        Optional<Player> player = getCorePlayer().getAsBukkitPlayer();

        if(player.isPresent() && player.get().isOnline()) {
            player.get().sendMessage(ChatColor.RED + "There was an issue loading your McRPG data, logging back into the server may fix this issue. If that does not fix the issue, please contact a server admin!");
        }
    }

    @Override
    protected void onDelayComplete() {
    }

    @Override
    protected void onIntervalStart() {
    }

    @Override
    protected void onIntervalPause() {
    }

    @Override
    protected void onIntervalResume() {
    }

    @Override
    public void onTaskExpire() {
    }
}