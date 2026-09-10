package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.QuestState;
import us.eunoians.mcrpg.quest.source.builtin.ManualQuestSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QuestInstanceDAOTest extends McRPGBaseTest {

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("Given the table already exists, when creating, then returns false")
        void attemptCreateTable_returnsFalse_whenTableExists() {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            when(db.tableExists(conn, "mcrpg_quest_instances")).thenReturn(true);

            boolean result = QuestInstanceDAO.attemptCreateTable(conn, db);

            assertFalse(result);
        }

        @Test
        @DisplayName("Given the table does not exist, when creating, then returns true")
        void attemptCreateTable_returnsTrue_whenTableDoesNotExist() throws SQLException {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(db.tableExists(conn, "mcrpg_quest_instances")).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            boolean result = QuestInstanceDAO.attemptCreateTable(conn, db);

            assertTrue(result);
            verify(ps).executeUpdate();
        }

        @Test
        @DisplayName("Given a SQL error during creation, when creating, then returns false")
        void attemptCreateTable_returnsFalse_whenSQLException() throws SQLException {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            when(db.tableExists(conn, "mcrpg_quest_instances")).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("test error"));

            boolean result = QuestInstanceDAO.attemptCreateTable(conn, db);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("saveQuestInstance")
    class SaveQuestInstance {

        @Test
        @DisplayName("Given a quest instance with all timestamps, when saving, then all parameters are bound")
        void saveQuestInstance_bindsAllParameters_whenTimestampsPresent() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            Instant startTime = Instant.ofEpochMilli(1000L);
            Instant endTime = Instant.ofEpochMilli(2000L);
            Instant expirationTime = Instant.ofEpochMilli(3000L);
            UUID questUUID = UUID.randomUUID();
            QuestInstance instance = new QuestInstance(
                    new NamespacedKey("mcrpg", "test_quest"),
                    questUUID,
                    new NamespacedKey("mcrpg", "single_player"),
                    QuestState.IN_PROGRESS,
                    null,
                    startTime,
                    endTime,
                    expirationTime,
                    new ManualQuestSource(),
                    null
            );

            List<PreparedStatement> statements = QuestInstanceDAO.saveQuestInstance(conn, instance);

            assertFalse(statements.isEmpty());
            verify(ps).setString(1, questUUID.toString());
            verify(ps).setString(2, "mcrpg:test_quest");
            verify(ps).setString(3, "IN_PROGRESS");
            verify(ps).setString(4, "mcrpg:single_player");
            verify(ps).setLong(5, 1000L);
            verify(ps).setLong(6, 2000L);
            verify(ps).setLong(7, 3000L);
        }

        @Test
        @DisplayName("Given a quest instance with null timestamps, when saving, then nulls are set correctly")
        void saveQuestInstance_setsNullTimestamps_whenTimestampsAbsent() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            QuestInstance instance = new QuestInstance(
                    new NamespacedKey("mcrpg", "null_time_quest"),
                    UUID.randomUUID(),
                    new NamespacedKey("mcrpg", "single_player"),
                    QuestState.NOT_STARTED,
                    null,
                    null,
                    null,
                    null,
                    new ManualQuestSource(),
                    null
            );

            QuestInstanceDAO.saveQuestInstance(conn, instance);

            verify(ps).setNull(eq(5), anyInt());
            verify(ps).setNull(eq(6), anyInt());
            verify(ps).setNull(eq(7), anyInt());
            verify(ps).setNull(eq(9), anyInt());
        }

        @Test
        @DisplayName("Given a quest with board rarity key, when saving, then rarity key is set as string")
        void saveQuestInstance_setsRarityKey_whenPresent() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            QuestInstance instance = new QuestInstance(
                    new NamespacedKey("mcrpg", "rarity_quest"),
                    UUID.randomUUID(),
                    new NamespacedKey("mcrpg", "single_player"),
                    QuestState.IN_PROGRESS,
                    null,
                    Instant.now(),
                    null,
                    null,
                    new ManualQuestSource(),
                    null
            );
            instance.setBoardRarityKey(new NamespacedKey("mcrpg", "rare"));

            QuestInstanceDAO.saveQuestInstance(conn, instance);

            verify(ps).setString(9, "mcrpg:rare");
        }

        @Test
        @DisplayName("Given a SQL exception during prepare, when saving, then returns empty list")
        void saveQuestInstance_returnsEmpty_whenSQLException() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("prepare failed"));

            QuestInstance instance = new QuestInstance(
                    new NamespacedKey("mcrpg", "fail_quest"),
                    UUID.randomUUID(),
                    new NamespacedKey("mcrpg", "single_player"),
                    QuestState.NOT_STARTED,
                    null,
                    null,
                    null,
                    null,
                    new ManualQuestSource(),
                    null
            );

            List<PreparedStatement> result = QuestInstanceDAO.saveQuestInstance(conn, instance);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteQuestInstance")
    class DeleteQuestInstance {

        @Test
        @DisplayName("Given a valid UUID, when deleting, then a prepared statement is returned")
        void deleteQuestInstance_returnsStatement() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            UUID questUUID = UUID.randomUUID();

            List<PreparedStatement> result = QuestInstanceDAO.deleteQuestInstance(conn, questUUID);

            assertFalse(result.isEmpty());
            verify(ps).setString(1, questUUID.toString());
        }

        @Test
        @DisplayName("Given a SQL exception during prepare, when deleting, then returns empty list")
        void deleteQuestInstance_returnsEmpty_whenSQLException() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("prepare failed"));

            List<PreparedStatement> result = QuestInstanceDAO.deleteQuestInstance(conn, UUID.randomUUID());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("bulkExpireStaleQuests")
    class BulkExpireStaleQuests {

        @Test
        @DisplayName("Given stale quests exist, when bulk expiring, then returns update count")
        void bulkExpireStaleQuests_returnsUpdateCount() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(5);

            int result = QuestInstanceDAO.bulkExpireStaleQuests(conn, 50000L);

            assertEquals(5, result);
            verify(ps).setString(1, "CANCELLED");
            verify(ps).setLong(2, 50000L);
            verify(ps).setString(3, "NOT_STARTED");
            verify(ps).setString(4, "IN_PROGRESS");
            verify(ps).setLong(5, 50000L);
        }

        @Test
        @DisplayName("Given a SQL exception, when bulk expiring, then returns zero")
        void bulkExpireStaleQuests_returnsZero_whenSQLException() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("update failed"));

            int result = QuestInstanceDAO.bulkExpireStaleQuests(conn, 50000L);

            assertEquals(0, result);
        }

        @Test
        @DisplayName("Given no stale quests, when bulk expiring, then returns zero")
        void bulkExpireStaleQuests_returnsZero_whenNoStaleQuests() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(0);

            int result = QuestInstanceDAO.bulkExpireStaleQuests(conn, 50000L);

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("loadQuestInstancesByState")
    class LoadQuestInstancesByState {

        @Test
        @DisplayName("Given no states provided, when loading, then returns empty list")
        void loadQuestInstancesByState_returnsEmpty_whenNoStates() {
            Connection conn = mock(Connection.class);

            List<QuestInstance> result = QuestInstanceDAO.loadQuestInstancesByState(conn);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given no matching rows, when loading by state, then returns empty list")
        void loadQuestInstancesByState_returnsEmpty_whenNoRows() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            List<QuestInstance> result = QuestInstanceDAO.loadQuestInstancesByState(conn, QuestState.IN_PROGRESS);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given multiple states, when loading, then binds all state parameters")
        void loadQuestInstancesByState_bindsAllStates() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            QuestInstanceDAO.loadQuestInstancesByState(conn, QuestState.NOT_STARTED, QuestState.IN_PROGRESS);

            verify(ps).setString(1, "NOT_STARTED");
            verify(ps).setString(2, "IN_PROGRESS");
        }

        @Test
        @DisplayName("Given a matching row with valid data, when loading by state, then returns quest instance")
        void loadQuestInstancesByState_returnsInstance_whenValidRow() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);

            UUID questUUID = UUID.randomUUID();
            when(rs.getString("quest_uuid")).thenReturn(questUUID.toString());
            when(rs.getString("definition_key")).thenReturn("mcrpg:test_quest");
            when(rs.getString("scope_type")).thenReturn("mcrpg:single_player");
            when(rs.getString("state")).thenReturn("IN_PROGRESS");
            when(rs.getLong("start_time")).thenReturn(1000L);
            when(rs.wasNull()).thenReturn(false, true, true);
            when(rs.getString("quest_source")).thenReturn("mcrpg:manual");
            when(rs.getString("board_rarity_key")).thenReturn(null);

            List<QuestInstance> result = QuestInstanceDAO.loadQuestInstancesByState(conn, QuestState.IN_PROGRESS);

            assertEquals(1, result.size());
            assertEquals(questUUID, result.get(0).getQuestUUID());
            assertEquals(QuestState.IN_PROGRESS, result.get(0).getQuestState());
        }

        @Test
        @DisplayName("Given a SQL exception during query, when loading by state, then returns empty list")
        void loadQuestInstancesByState_returnsEmpty_whenSQLException() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("query failed"));

            List<QuestInstance> result = QuestInstanceDAO.loadQuestInstancesByState(conn, QuestState.IN_PROGRESS);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("loadQuestInstance")
    class LoadQuestInstance {

        @Test
        @DisplayName("Given a valid row, when loading, then returns populated quest instance")
        void loadQuestInstance_returnsInstance_whenValidRow() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);

            when(rs.getString("definition_key")).thenReturn("mcrpg:load_test");
            when(rs.getString("scope_type")).thenReturn("mcrpg:single_player");
            when(rs.getString("state")).thenReturn("COMPLETED");
            when(rs.getLong("start_time")).thenReturn(1000L);
            when(rs.getLong("end_time")).thenReturn(2000L);
            when(rs.getLong("expiration_time")).thenReturn(0L);
            when(rs.wasNull()).thenReturn(false, false, true);
            when(rs.getString("quest_source")).thenReturn("mcrpg:manual");
            when(rs.getString("board_rarity_key")).thenReturn("mcrpg:common");

            UUID questUUID = UUID.randomUUID();
            Optional<QuestInstance> result = QuestInstanceDAO.loadQuestInstance(conn, questUUID);

            assertTrue(result.isPresent());
            QuestInstance quest = result.get();
            assertEquals(questUUID, quest.getQuestUUID());
            assertEquals(QuestState.COMPLETED, quest.getQuestState());
            assertTrue(quest.getBoardRarityKey().isPresent());
            assertEquals(new NamespacedKey("mcrpg", "common"), quest.getBoardRarityKey().get());
        }

        @Test
        @DisplayName("Given no matching row, when loading, then returns empty")
        void loadQuestInstance_returnsEmpty_whenNoRow() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            Optional<QuestInstance> result = QuestInstanceDAO.loadQuestInstance(conn, UUID.randomUUID());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given a SQL exception, when loading, then returns empty")
        void loadQuestInstance_returnsEmpty_whenSQLException() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("load failed"));

            Optional<QuestInstance> result = QuestInstanceDAO.loadQuestInstance(conn, UUID.randomUUID());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("loadScopeType")
    class LoadScopeType {

        @Test
        @DisplayName("Given a valid scope_type key, when loading, then returns the key")
        void loadScopeType_validKey_returnsKey() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("scope_type")).thenReturn("mcrpg:single_player");

            Optional<NamespacedKey> result = QuestInstanceDAO.loadScopeType(conn, UUID.randomUUID());

            assertTrue(result.isPresent());
            assertEquals(new NamespacedKey("mcrpg", "single_player"), result.get());
        }

        @Test
        @DisplayName("Given no matching row, when loading scope type, then returns empty")
        void loadScopeType_noRow_returnsEmpty() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            Optional<NamespacedKey> result = QuestInstanceDAO.loadScopeType(conn, UUID.randomUUID());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given a malformed scope_type, when loading, then returns empty")
        void loadScopeType_malformedKey_returnsEmpty() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("scope_type")).thenReturn("!!!invalid!!!");

            Optional<NamespacedKey> result = QuestInstanceDAO.loadScopeType(conn, UUID.randomUUID());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Given a SQL exception, when loading scope type, then returns empty")
        void loadScopeType_returnsEmpty_whenSQLException() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("query failed"));

            Optional<NamespacedKey> result = QuestInstanceDAO.loadScopeType(conn, UUID.randomUUID());

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("saveFullQuestTree")
    class SaveFullQuestTree {

        @Test
        @DisplayName("Given a quest instance, when saving full tree, then prepared statements are returned")
        void saveFullQuestTree_returnsPreparedStatements() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            QuestInstance instance = new QuestInstance(
                    new NamespacedKey("mcrpg", "tree_quest"),
                    UUID.randomUUID(),
                    new NamespacedKey("mcrpg", "single_player"),
                    QuestState.IN_PROGRESS,
                    null,
                    Instant.now(),
                    null,
                    null,
                    new ManualQuestSource(),
                    null
            );

            List<PreparedStatement> statements = QuestInstanceDAO.saveFullQuestTree(conn, instance);
            assertNotNull(statements);
            assertFalse(statements.isEmpty());
        }

        @Test
        @DisplayName("Given a quest instance, when saving full tree, then the connection is used")
        void saveFullQuestTree_usesConnection() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            QuestInstance instance = new QuestInstance(
                    new NamespacedKey("mcrpg", "dao_test"),
                    UUID.randomUUID(),
                    new NamespacedKey("mcrpg", "single_player"),
                    QuestState.COMPLETED,
                    null,
                    Instant.ofEpochMilli(1000L),
                    Instant.ofEpochMilli(2000L),
                    null,
                    new ManualQuestSource(),
                    null
            );

            QuestInstanceDAO.saveFullQuestTree(conn, instance);
            verify(conn).prepareStatement(anyString());
        }
    }
}
