package us.eunoians.mcrpg;

import com.diamonddagger590.mccore.CorePlugin;
import com.diamonddagger590.mccore.command.DisplayNameCommand;
import com.diamonddagger590.mccore.command.LoreCommand;
import com.diamonddagger590.mccore.database.table.impl.MutexDAO;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.player.PlayerManager;
import com.jeff_media.customblockdata.CustomBlockData;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Bukkit;
import org.geysermc.api.Geyser;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.AbilityRegistry;
import us.eunoians.mcrpg.ability.attribute.AbilityAttributeManager;
import us.eunoians.mcrpg.ability.impl.swords.bleed.BleedManager;
import us.eunoians.mcrpg.command.TestGuiCommand;
import us.eunoians.mcrpg.command.admin.DebugCommand;
import us.eunoians.mcrpg.command.admin.ReloadPluginCommand;
import us.eunoians.mcrpg.command.admin.reset.ResetPlayerCommand;
import us.eunoians.mcrpg.command.admin.reset.ResetSkillCommand;
import us.eunoians.mcrpg.command.give.GiveExperienceCommand;
import us.eunoians.mcrpg.command.give.GiveLevelsCommand;
import us.eunoians.mcrpg.command.link.LinkChestCommand;
import us.eunoians.mcrpg.command.link.UnlinkChestCommand;
import us.eunoians.mcrpg.command.loadout.LoadoutCommand;
import us.eunoians.mcrpg.command.loadout.LoadoutEditCommand;
import us.eunoians.mcrpg.command.loadout.LoadoutSetCommand;
import us.eunoians.mcrpg.command.quest.TestQuestStartCommand;
import us.eunoians.mcrpg.configuration.FileManager;
import us.eunoians.mcrpg.database.McRPGDatabaseManager;
import us.eunoians.mcrpg.database.table.PlayerLoadoutDAO;
import us.eunoians.mcrpg.database.table.SkillDAO;
import us.eunoians.mcrpg.display.DisplayManager;
import us.eunoians.mcrpg.entity.EntityManager;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.expansion.ContentExpansionManager;
import us.eunoians.mcrpg.expansion.McRPGExpansion;
import us.eunoians.mcrpg.expansion.handler.ContentHandlerType;
import us.eunoians.mcrpg.listener.ability.OnAbilityActivateListener;
import us.eunoians.mcrpg.listener.ability.OnAbilityCooldownExpireListener;
import us.eunoians.mcrpg.listener.ability.OnAbilityPutOnCooldownListener;
import us.eunoians.mcrpg.listener.ability.OnAbilityUnlockListener;
import us.eunoians.mcrpg.listener.ability.OnAttackAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnBleedActivateListener;
import us.eunoians.mcrpg.listener.ability.OnBlockBreakListener;
import us.eunoians.mcrpg.listener.ability.OnBlockDropItemListener;
import us.eunoians.mcrpg.listener.ability.OnExtraOreActivateListener;
import us.eunoians.mcrpg.listener.ability.OnInteractAbilityListener;
import us.eunoians.mcrpg.listener.ability.OnSneakAbilityListener;
import us.eunoians.mcrpg.listener.entity.OnAbilityHolderReadyListener;
import us.eunoians.mcrpg.listener.entity.OnAbilityHolderUnreadyListener;
import us.eunoians.mcrpg.listener.entity.player.CorePlayerLoadListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerJoinListener;
import us.eunoians.mcrpg.listener.entity.player.PlayerLeaveListener;
import us.eunoians.mcrpg.listener.quest.QuestCompleteListener;
import us.eunoians.mcrpg.listener.quest.QuestObjectiveCompleteListener;
import us.eunoians.mcrpg.listener.skill.OnAttackLevelListener;
import us.eunoians.mcrpg.listener.skill.OnBlockBreakLevelListener;
import us.eunoians.mcrpg.listener.skill.OnSkillLevelUpListener;
import us.eunoians.mcrpg.listener.world.BlockPlaceListener;
import us.eunoians.mcrpg.papi.McRPGPapiExpansion;
import us.eunoians.mcrpg.quest.QuestManager;
import us.eunoians.mcrpg.skill.SkillRegistry;
import us.eunoians.mcrpg.util.LunarUtils;

import java.sql.Connection;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * The main class for McRPG where developers should be able to access various components of the API's provided by McRPG
 */
public class McRPG extends CorePlugin {

    private static final int id = 6386;

    //Needed to support McMMO's Healthbars
    private static final String customNameKey = "mcMMO: Custom Name";
    private static final String customVisibleKey = "mcMMO: Name Visibility";

    private FileManager fileManager;

    private AbilityRegistry abilityRegistry;
    private SkillRegistry skillRegistry;
    private AbilityAttributeManager abilityAttributeManager;
    private EntityManager entityManager;
    private DisplayManager displayManager;
    private QuestManager questManager;
    private BleedManager bleedManager;
    private ContentExpansionManager contentExpansionManager;

    private GlowingBlocks glowingBlocks;
    private GlowingEntities glowingEntities;

    private boolean healthBarPluginEnabled = false;
    private boolean mvdwEnabled = false;
    private boolean papiEnabled = false;
    private boolean ncpEnabled = false;
    private boolean sickleEnabled = false;
    private boolean worldGuardEnabled = false;
    private boolean mcmmoEnabled = false;
    private boolean geyserEnabled = false;
    private boolean lunarEnabled = false;

    @Override
    public void onEnable() {
        super.onEnable();
        if (!isUnitTest()) {
            initializeFiles();
            glowingBlocks = new GlowingBlocks(this);
            glowingEntities = new GlowingEntities(this);
        }

        entityManager = new EntityManager(this);
        playerManager = new PlayerManager(this);

        abilityRegistry = new AbilityRegistry(this);
        skillRegistry = new SkillRegistry(this);

        abilityAttributeManager = new AbilityAttributeManager(this);
        displayManager = new DisplayManager();
        questManager = new QuestManager();
        bleedManager = new BleedManager(this);
        contentExpansionManager = new ContentExpansionManager(this);

        if (!isUnitTest()) {
            registerNativeExpansions();
        }

        setupHooks();
        if (!isUnitTest()) {
            initializeDatabase();
            registerListeners();
            constructCommands();
            reloadableContentRegistry.reloadAllContent();
        }
    }

    @Override
    public void onDisable() {

        if (!isUnitTest()) {
            glowingBlocks.disable();
            glowingEntities.disable();
            Connection connection = databaseManager.getDatabase().getConnection();
            if (connection == null) {
                throw new RuntimeException("Database was not available on shutdown... there is likely lost McRPG data as a result.");
            } else {
                for (CorePlayer corePlayer : playerManager.getAllPlayers()) {
                    if (corePlayer instanceof McRPGPlayer mcRPGPlayer) {
                        try {
                            // TODO make this one thing so it isnt in two spots
                            SkillDAO.savePlayerSkillData(connection, mcRPGPlayer.asSkillHolder()).get();
                            PlayerLoadoutDAO.saveAllPlayerLoadouts(connection, mcRPGPlayer.asSkillHolder()).get();
                            if (isLunarEnabled()) {
                                LunarUtils.clearCooldowns(mcRPGPlayer.getUUID());
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                        if (mcRPGPlayer.useMutex()) {
                            MutexDAO.updateUserMutex(connection, mcRPGPlayer.getUUID(), false);
                        }
                    }
                }
            }
        }
        super.onDisable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeDatabase() {
        this.databaseManager = new McRPGDatabaseManager(this);
        this.databaseManager.initializeDatabase();
    }

    /**
     * Initializes the {@link FileManager} and populates it with all files McRPG needs
     */
    private void initializeFiles() {
        fileManager = new FileManager(this);
    }

    @Override
    public void constructCommands() {
        super.constructCommands();
        TestGuiCommand.registerCommand();

        LoadoutCommand.registerCommand();
        LoadoutEditCommand.registerCommand();
        LoadoutSetCommand.registerCommand();

        // Give Commands
        GiveLevelsCommand.registerCommand();
        GiveExperienceCommand.registerCommand();

        // Reset commands
        ResetSkillCommand.registerCommand();
        ResetPlayerCommand.registerCommand();

        // Debug Command
        DebugCommand.registerCommand();

        // Quest Command
        TestQuestStartCommand.registerCommand();

        // Reload command
        ReloadPluginCommand.registerCommand();

        // Link commands
        LinkChestCommand.registerCommand();
        UnlinkChestCommand.registerCommand();

        // Test commands
        LoreCommand.registerCommand();
        DisplayNameCommand.registerCommand();
    }

    @Override
    public void registerListeners() {
        // Register core listeners
        super.registerListeners();

        // Player load/save
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerLeaveListener(), this);
        Bukkit.getPluginManager().registerEvents(new CorePlayerLoadListener(), this);

        // Ability activation/ready listeners
        Bukkit.getPluginManager().registerEvents(new OnAttackAbilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnBleedActivateListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnInteractAbilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnSneakAbilityListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnBlockBreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnBlockDropItemListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnExtraOreActivateListener(), this);

        // Skill listeners
        Bukkit.getPluginManager().registerEvents(new OnSkillLevelUpListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnAttackLevelListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnBlockBreakLevelListener(), this);

        // Ability listeners
        Bukkit.getPluginManager().registerEvents(new OnAbilityHolderReadyListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnAbilityHolderUnreadyListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnAbilityUnlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnAbilityCooldownExpireListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnAbilityPutOnCooldownListener(), this);

        // Quest Listeners
        Bukkit.getPluginManager().registerEvents(new QuestCompleteListener(), this);
        Bukkit.getPluginManager().registerEvents(new QuestObjectiveCompleteListener(), this);

        // World listener
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(), this);
        CustomBlockData.registerListener(this);

        // Debug Listener
        Bukkit.getPluginManager().registerEvents(new OnAbilityActivateListener(), this);
    }

    /**
     * Setup 3rd party plugin hooks that are natively supported by McRPG
     */
    private void setupHooks() {

        healthBarPluginEnabled = getServer().getPluginManager().getPlugin("HealthBar") != null;
        sickleEnabled = getServer().getPluginManager().getPlugin("Sickle") != null;

        if (healthBarPluginEnabled) {
            getLogger().info("HealthBar plugin found, McRPG's healthbars are automatically disabled.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            papiEnabled = true;
            getLogger().info("Papi PlaceholderAPI found... registering hooks");
            new McRPGPapiExpansion(this).register();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
            ncpEnabled = true;
            getLogger().info("NoCheatPlus found... will enable anticheat support");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            mcmmoEnabled = true;
            getLogger().info("McMMO found... ready to convert.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            worldGuardEnabled = true;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Geyser")) {
            geyserEnabled = true;
            getLogger().info("Geyser found... enabling support.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Apollo-Bukkit")) {
            lunarEnabled = true;
            getLogger().info("Apollo found... enabling Lunar Client support.");
        }
    }

    /**
     * Registers the native {@link us.eunoians.mcrpg.expansion.ContentExpansion}s for McRPG
     */
    private void registerNativeExpansions() {
        Arrays.stream(ContentHandlerType.values()).forEach(contentHandlerType -> contentExpansionManager.registerContentHandler(contentHandlerType.getContentHandler()));
        contentExpansionManager.registerContentExpansion(new McRPGExpansion(this));
    }

    /**
     * Get the {@link FileManager} used by McRPG
     *
     * @return The {@link FileManager} used by McRPG
     */
    @NotNull
    public FileManager getFileManager() {
        return fileManager;
    }

    /**
     * Get the {@link EntityManager} used by McRPG
     *
     * @return The {@link EntityManager} used by McRPG
     */
    @NotNull
    public EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    @NotNull
    public McRPGDatabaseManager getDatabaseManager() {
        return (McRPGDatabaseManager) databaseManager;
    }

    /**
     * Gets the {@link AbilityRegistry} used by McRPG
     *
     * @return The {@link AbilityRegistry} used by McRPG
     */
    @NotNull
    public AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    /**
     * Gets the {@link SkillRegistry} used by McRPG
     *
     * @return The {@link SkillRegistry} used by McRPG
     */
    @NotNull
    public SkillRegistry getSkillRegistry() {
        return skillRegistry;
    }

    /**
     * Gets the {@link AbilityAttributeManager} used by McRPG
     *
     * @return The {@link AbilityAttributeManager} used by McRPG
     */
    @NotNull
    public AbilityAttributeManager getAbilityAttributeManager() {
        return abilityAttributeManager;
    }

    /**
     * Gets the {@link DisplayManager} used by McRPG
     *
     * @return The {@link DisplayManager} used by McRPG
     */
    @NotNull
    public DisplayManager getDisplayManager() {
        return displayManager;
    }

    /**
     * Gets the {@link QuestManager} used by McRPG
     *
     * @return The {@link QuestManager} used by McRPG
     */
    @NotNull
    public QuestManager getQuestManager() {
        return questManager;
    }

    /**
     * Gets the {@link GlowingBlocks} used by McRPG.
     *
     * @return The {@link GlowingBlocks} used by McRPG.
     */
    @NotNull
    public GlowingBlocks getGlowingBlocks() {
        return glowingBlocks;
    }

    /**
     * Gets the {@link GlowingEntities} used by McRPG.
     *
     * @return The {@link GlowingEntities} used by McRPG.
     */
    @NotNull
    public GlowingEntities getGlowingEntities() {
        return glowingEntities;
    }

    /**
     * Gets the {@link BleedManager} used by McRPG.
     *
     * @return The {@link BleedManager} used by McRPG.
     */
    @NotNull
    public BleedManager getBleedManager() {
        return bleedManager;
    }

    /**
     * Gets the {@link ContentExpansionManager} used by McRPG.
     *
     * @return The {@link ContentExpansionManager} used by McRPG.
     */
    @NotNull
    public ContentExpansionManager getContentExpansionManager() {
        return contentExpansionManager;
    }

    /**
     * Checks to see if Lunar Client support is enabled.
     *
     * @return {@code true} if Lunar Client support is enabled
     */
    public boolean isLunarEnabled() {
        return lunarEnabled;
    }

    /**
     * Checks to see if Geyser is enabled and registered.
     *
     * @return {@code true} if Geyser is enabled and registered.
     */
    public boolean isGeyserEnabled() {
        return geyserEnabled && Geyser.isRegistered();
    }

    public boolean isPapiEnabled() {
        return papiEnabled;
    }

    @NotNull
    public static McRPG getInstance() {
        return (McRPG) CorePlugin.getInstance();
    }
}
