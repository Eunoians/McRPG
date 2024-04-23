package us.eunoians.mcrpg.command;

import com.diamonddagger590.mccore.player.PlayerManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.CommandManager;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.AbilityGui;
import us.eunoians.mcrpg.gui.HomeGui;
import us.eunoians.mcrpg.gui.UpgradeAbilityGui;

public class TestGuiCommand {

    public static void registerCommand() {
        CommandManager<CommandSender> commandManager = McRPG.getInstance().getCommandManager();
        commandManager.command(commandManager.commandBuilder("test").handler(commandContext -> {
            CommandSender commandSender = commandContext.sender();
            if (commandSender instanceof Player player) {
                PlayerManager playerManager = McRPG.getInstance().getPlayerManager();
                playerManager.getPlayer(player.getUniqueId()).ifPresent(corePlayer -> {
                    if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
                        HomeGui homeGui = new HomeGui(mcRPGPlayer);
                        McRPG.getInstance().getGuiTracker().trackPlayerGui(player, homeGui);
                        player.openInventory(homeGui.getInventory());
                    }
                });
            }
        }));

        commandManager.command(commandManager.commandBuilder("skill").handler(commandContext -> {
            CommandSender commandSender = commandContext.sender();
            if (commandSender instanceof Player player) {
                PlayerManager playerManager = McRPG.getInstance().getPlayerManager();
                playerManager.getPlayer(player.getUniqueId()).ifPresent(corePlayer -> {
                    if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
                        AbilityGui abilityGui = new AbilityGui(mcRPGPlayer);
                        McRPG.getInstance().getGuiTracker().trackPlayerGui(player, abilityGui);
                        player.openInventory(abilityGui.getInventory());
                    }
                });
            }
        }));

        commandManager.command(commandManager.commandBuilder("upgrade").handler(commandContext -> {
            CommandSender commandSender = commandContext.sender();
            if (commandSender instanceof Player player) {
                PlayerManager playerManager = McRPG.getInstance().getPlayerManager();
                playerManager.getPlayer(player.getUniqueId()).ifPresent(corePlayer -> {
                    if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
                        UpgradeAbilityGui abilityGui = new UpgradeAbilityGui(mcRPGPlayer);
                        McRPG.getInstance().getGuiTracker().trackPlayerGui(player, abilityGui);
                        player.openInventory(abilityGui.getInventory());
                    }
                });
            }
        }));
    }
}
