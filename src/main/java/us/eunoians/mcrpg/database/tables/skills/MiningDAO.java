package us.eunoians.mcrpg.database.tables.skills;

import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.database.DatabaseManager;
import us.eunoians.mcrpg.database.tables.TableVersionHistoryDAO;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.Skills;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * A DAO used to store data regarding a player's {@link us.eunoians.mcrpg.skills.Mining} skill
 *
 * @author DiamondDagger590
 */
public class MiningDAO extends SkillDAO {

    private static final String TABLE_NAME = "mcrpg_mining_data";
    private static final int CURRENT_TABLE_VERSION = 1;

    private static boolean isAcceptingQueries = true;

    /**
     * Attempts to create a new table for this DAO provided that the table does not already exist.
     *
     * @param connection      The {@link Connection} to use to attempt the creation
     * @param databaseManager The {@link DatabaseManager} being used to attempt to create the table
     * @return A {@link CompletableFuture} containing a {@link Boolean} that is {@code true} if a new table was made,
     * or {@code false} otherwise.
     */
    public static CompletableFuture<Boolean> attemptCreateTable(Connection connection, DatabaseManager databaseManager) {

        CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            //Check to see if the table already exists
            if (databaseManager.getDatabase().tableExists(TABLE_NAME)) {
                completableFuture.complete(false);
                return;
            }

            isAcceptingQueries = false;

            /*****
             ** Table Description:
             ** Contains player data for the archery skill
             *
             *
             * id is the id of the entry which auto increments but doesn't really serve a large purpose since it isn't
             * guaranteed to be the same for players across the board
             * uuid is the {@link java.util.UUID} of the player being stored
             * current_exp is the amount of exp a player currently has in this skill
             * current_level is the level a player currently has in this skill
             * is_double_drop_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.mining.DoubleDrop} ability toggled
             * is_richer_ores_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.mining.RicherOres} ability toggled
             * is_remote_transfer_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.mining.RemoteTransfer} ability toggled
             * is_its_a_triple_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.mining.ItsATriple} ability toggled
             * is_super_breaker_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.mining.SuperBreaker} ability toggled
             * is_blast_mining_toggled represents if the player has the {@link us.eunoians.mcrpg.abilities.mining.BlastMining} ability toggled
             * is_ore_scanner_toggled represents if the player ahs the {@link us.eunoians.mcrpg.abilities.mining.OreScanner} ability toggled
             * richer_ores_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.mining.RicherOres} ability
             * remote_transfer_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.mining.RemoteTransfer} ability
             * its_a_triple_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.mining.ItsATriple} ability
             * super_breaker_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.mining.SuperBreaker} ability
             * blast_mining_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.mining.BlastMining} ability
             * ore_scanner_tier represents the tier for the player's {@link us.eunoians.mcrpg.abilities.mining.OreScanner} ability
             * is_richer_ores_pending represents if the player has {@link us.eunoians.mcrpg.abilities.mining.RicherOres} pending to be accepted
             * is_remote_transfer_pending represents if the player has {@link us.eunoians.mcrpg.abilities.mining.RemoteTransfer} pending to be accepted
             * is_its_a_triple_pending represents if the player has {@link us.eunoians.mcrpg.abilities.mining.ItsATriple} pending to be accepted
             * is_super_breaker_pending represents if the player has {@link us.eunoians.mcrpg.abilities.mining.SuperBreaker} pending to be accepted
             * is_blast_mining_pending represents if the player has {@link us.eunoians.mcrpg.abilities.mining.BlastMining} pending to be accepted
             * is_ore_scanner_pending represents if the player has {@link us.eunoians.mcrpg.abilities.mining.OreScanner} pending to be accepted
             * super_breaker_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.mining.SuperBreaker} ability
             * blast_mining_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.mining.BlastMining} ability
             * ore_scanner_cooldown represents the cooldown for the player's {@link us.eunoians.mcrpg.abilities.mining.OreScanner} ability
             **
             ** Reasoning for structure:
             ** PK is the `uuid` field, as each player only has one uuid
             *****/
            try (PreparedStatement statement = connection.prepareStatement("CREATE TABLE `" + TABLE_NAME + "`" +
                                                                           "(" +
                                                                           "`id` int(11) NOT NULL AUTO_INCREMENT," +
                                                                           "`uuid` varchar(32) NOT NULL," +
                                                                           "`current_exp` int(11) NOT NULL DEFAULT 0," +
                                                                           "`current_level` int(11) NOT NULL DEFAULT 0," +
                                                                           "`is_double_drop_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_richer_ores_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_remote_transfer_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_its_a_triple_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_super_breaker_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_blast_mining_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`is_ore_scanner_toggled` BIT NOT NULL DEFAULT 1," +
                                                                           "`richer_ores_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`remote_transfer_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`its_a_triple_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`super_breaker_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`blast_mining_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`ore_scanner_tier` int(11) NOT NULL DEFAULT 0," +
                                                                           "`is_richer_ores_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_remote_transfer_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_its_a_triple_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_super_breaker_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_blast_mining_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`is_ore_scanner_pending` BIT NOT NULL DEFAULT 0," +
                                                                           "`super_breaker_cooldown` int(11) NOT NULL DEFAULT 0," +
                                                                           "`blast_mining_cooldown` int(11) NOT NULL DEFAULT 0," +
                                                                           "`ore_scanner_cooldown` int(11) NOT NULL DEFAULT 0," +
                                                                           "PRIMARY KEY (`uuid`)" +
                                                                           ");")) {
                statement.executeUpdate();
            }
            catch (SQLException e) {
                e.printStackTrace();
                completableFuture.completeExceptionally(e);
            }

            isAcceptingQueries = true;

            completableFuture.complete(true);
        });

        return completableFuture;
    }

    /**
     * Checks to see if there are any version differences from the live version of this SQL table and then current version.
     * <p>
     * If there are any differences, it will iteratively go through and update through each version to ensure the database is
     * safe to run queries on.
     *
     * @param connection The {@link Connection} that will be used to run the changes
     * @return The {@link  CompletableFuture} that is running these changes.
     */
    public static CompletableFuture<Void> updateTable(Connection connection) {

        DatabaseManager databaseManager = McRPG.getInstance().getDatabaseManager();
        CompletableFuture<Void> completableFuture = new CompletableFuture<>();

        databaseManager.getDatabaseExecutorService().submit(() -> {

            if (TableVersionHistoryDAO.isAcceptingQueries()) {

                TableVersionHistoryDAO.getLatestVersion(connection, TABLE_NAME).thenAccept(lastStoredVersion -> {

                    if (lastStoredVersion >= CURRENT_TABLE_VERSION) {
                        completableFuture.complete(null);
                        return;
                    }

                    isAcceptingQueries = false;

                    //This is where we would add any updates but we don't have any
                    if (lastStoredVersion == 1) { //Would be used whenever our CURRENT_TABLE_VERSION is 2

                    }

                    isAcceptingQueries = true;
                });

            }

            completableFuture.complete(null);
        });

        return completableFuture;
    }

    /**
     * Gets a {@link SkillDataSnapshot} containing all of the player's skill data for {@link us.eunoians.mcrpg.skills.Mining}. If
     * the provided {@link UUID} doesn't have any data, any empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     *
     * @param connection The {@link Connection} to use to get the skill data
     * @param uuid       The {@link UUID} of the player who's data is being obtained
     * @return A {@link CompletableFuture} containing a {@link SkillDataSnapshot} that has all of the player's {@link us.eunoians.mcrpg.skills.Mining} skill
     * data. If the provided {@link UUID} doesn't have any data, any empty {@link SkillDataSnapshot} will be returned instead with no populated maps
     * and default exp/level values set to 0
     */
    public static CompletableFuture<SkillDataSnapshot> getPlayerMiningData(Connection connection, UUID uuid) {
        return getSkillData(TABLE_NAME, connection, uuid, Skills.MINING);
    }

    //TODO because I only care about loading player data rn and cba to save it
    public static void savePlayerMiningnData(Connection connection, McRPGPlayer mcRPGPlayer) {

    }

    /**
     * Checks to see if this table is accepting queries at the moment. A reason it could be false is either the table is
     * in creation or the table is being updated and for some reason a query is attempting to be run.
     *
     * @return {@code true} if this table is accepting queries
     */
    public static boolean isAcceptingQueries() {
        return isAcceptingQueries;
    }
}