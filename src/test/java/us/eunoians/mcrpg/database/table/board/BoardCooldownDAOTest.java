package us.eunoians.mcrpg.database.table.board;

import com.diamonddagger590.mccore.database.Database;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;

import com.diamonddagger590.mccore.util.TimeProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class BoardCooldownDAOTest extends McRPGBaseTest {

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("Returns false when table already exists")
        void returnsFalse_whenTableExists() {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabase.tableExists(mockConnection, BoardCooldownDAO.TABLE_NAME)).thenReturn(true);

            boolean result = BoardCooldownDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("Returns true when table does not exist and is created")
        void returnsTrue_whenTableCreated() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockDatabase.tableExists(mockConnection, BoardCooldownDAO.TABLE_NAME)).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = BoardCooldownDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
            verify(mockStatement).executeUpdate();
        }

        @Test
        @DisplayName("Returns false when table creation throws SQLException")
        void returnsFalse_whenSQLExceptionOnCreate() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabase.tableExists(mockConnection, BoardCooldownDAO.TABLE_NAME)).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("creation error"));

            boolean result = BoardCooldownDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("saveCooldown")
    class SaveCooldown {

        @Test
        @DisplayName("Returns prepared statements with non-null keys")
        void returnsPreparedStatements_withNonNullKeys() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            List<PreparedStatement> statements = BoardCooldownDAO.saveCooldown(
                    mockConnection,
                    "cooldown-1",
                    "rotation",
                    "player",
                    UUID.randomUUID().toString(),
                    new NamespacedKey("mcrpg", "mine_stone"),
                    new NamespacedKey("mcrpg", "daily_personal"),
                    1000000L
            );

            assertNotNull(statements);
            assertFalse(statements.isEmpty());
            verify(mockStatement).setString(eq(5), eq("mcrpg:mine_stone"));
            verify(mockStatement).setString(eq(6), eq("mcrpg:daily_personal"));
        }

        @Test
        @DisplayName("Handles null keys by setting SQL NULL")
        void handlesNullKeys() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            BoardCooldownDAO.saveCooldown(
                    mockConnection,
                    "cooldown-2",
                    "rotation",
                    "land",
                    "land-uuid-123",
                    null,
                    null,
                    2000000L
            );

            verify(mockStatement).setNull(eq(5), eq(Types.VARCHAR));
            verify(mockStatement).setNull(eq(6), eq(Types.VARCHAR));
        }

        @Test
        @DisplayName("Sets cooldown ID and expiration correctly")
        void setsCooldownIdAndExpiration() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            String cooldownId = "cd-abc-123";
            long expiresAt = 9999999L;
            BoardCooldownDAO.saveCooldown(
                    mockConnection,
                    cooldownId,
                    "quest_repeat",
                    "player",
                    "player-uuid",
                    null,
                    null,
                    expiresAt
            );

            verify(mockStatement).setString(1, cooldownId);
            verify(mockStatement).setLong(7, expiresAt);
        }

        @Test
        @DisplayName("Returns empty list on SQLException")
        void returnsEmptyList_onSQLException() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

            List<PreparedStatement> result = BoardCooldownDAO.saveCooldown(
                    mockConnection, "id", "type", "scope", "ident", null, null, 0L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("isOnCooldown")
    class IsOnCooldown {

        @Test
        @DisplayName("Returns false when no results")
        void returnsFalse_whenNoResults() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            boolean result = BoardCooldownDAO.isOnCooldown(
                    mockConnection,
                    "rotation",
                    "player",
                    UUID.randomUUID().toString(),
                    null,
                    null
            );

            assertFalse(result);
        }

        @Test
        @DisplayName("Returns true when result present")
        void returnsTrue_whenPresent() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true);

            boolean result = BoardCooldownDAO.isOnCooldown(
                    mockConnection,
                    "rotation",
                    "player",
                    UUID.randomUUID().toString(),
                    null,
                    null
            );

            assertTrue(result);
        }

        @Test
        @DisplayName("Binds questDefinitionKey when non-null")
        void bindsQuestDefinitionKey_whenNonNull() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            NamespacedKey questDefKey = new NamespacedKey("mcrpg", "test_quest");
            BoardCooldownDAO.isOnCooldown(
                    mockConnection,
                    "quest_repeat",
                    "player",
                    "player-uuid",
                    questDefKey,
                    null
            );

            verify(mockStatement).setString(eq(5), eq("mcrpg:test_quest"));
        }

        @Test
        @DisplayName("Binds categoryKey when non-null")
        void bindsCategoryKey_whenNonNull() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            NamespacedKey categoryKey = new NamespacedKey("mcrpg", "daily_shared");
            BoardCooldownDAO.isOnCooldown(
                    mockConnection,
                    "category_rotation",
                    "player",
                    "player-uuid",
                    null,
                    categoryKey
            );

            verify(mockStatement).setString(eq(5), eq("mcrpg:daily_shared"));
        }

        @Test
        @DisplayName("Binds both keys when both non-null")
        void bindsBothKeys_whenBothNonNull() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            NamespacedKey questDefKey = new NamespacedKey("mcrpg", "quest_1");
            NamespacedKey categoryKey = new NamespacedKey("mcrpg", "cat_1");
            BoardCooldownDAO.isOnCooldown(
                    mockConnection,
                    "quest_repeat",
                    "player",
                    "player-uuid",
                    questDefKey,
                    categoryKey
            );

            verify(mockStatement).setString(eq(5), eq("mcrpg:quest_1"));
            verify(mockStatement).setString(eq(6), eq("mcrpg:cat_1"));
        }

        @Test
        @DisplayName("Binds current time from TimeProvider")
        void bindsCurrentTime_fromTimeProvider() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            TimeProvider timeProvider = mcRPG.getTimeProvider();
            Instant fixedInstant = Instant.ofEpochMilli(5000L);
            when(timeProvider.now()).thenReturn(fixedInstant);

            BoardCooldownDAO.isOnCooldown(
                    mockConnection, "rotation", "player", "player-uuid", null, null);

            verify(mockStatement).setLong(4, 5000L);
        }

        @Test
        @DisplayName("Returns false on SQLException")
        void returnsFalse_onSQLException() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

            boolean result = BoardCooldownDAO.isOnCooldown(
                    mockConnection, "type", "scope", "ident", null, null);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("listCooldowns")
    class ListCooldowns {

        @Test
        @DisplayName("Returns empty list when no rows")
        void returnsEmptyList_whenNoRows() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            List<BoardCooldownDAO.CooldownRecord> result = BoardCooldownDAO.listCooldowns(
                    mockConnection, "player", "player-uuid");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Returns records when rows exist")
        void returnsRecords_whenRowsExist() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, true, false);
            when(mockResultSet.getString("cooldown_type")).thenReturn("quest_repeat", "category_rotation");
            when(mockResultSet.getString("quest_definition_key")).thenReturn("mcrpg:quest_1", (String) null);
            when(mockResultSet.getString("category_key")).thenReturn((String) null, "mcrpg:daily");
            when(mockResultSet.getLong("expires_at")).thenReturn(5000000L, 6000000L);

            List<BoardCooldownDAO.CooldownRecord> result = BoardCooldownDAO.listCooldowns(
                    mockConnection, "player", "player-uuid");

            assertEquals(2, result.size());
            assertEquals("quest_repeat", result.get(0).cooldownType());
            assertEquals("mcrpg:quest_1", result.get(0).questDefinitionKey());
            assertNull(result.get(0).categoryKey());
            assertEquals(5000000L, result.get(0).expiresAt());

            assertEquals("category_rotation", result.get(1).cooldownType());
            assertNull(result.get(1).questDefinitionKey());
            assertEquals("mcrpg:daily", result.get(1).categoryKey());
            assertEquals(6000000L, result.get(1).expiresAt());
        }

        @Test
        @DisplayName("Binds scope parameters correctly")
        void bindsScopeParameters() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            BoardCooldownDAO.listCooldowns(mockConnection, "entity", "land-123");

            verify(mockStatement).setString(1, "entity");
            verify(mockStatement).setString(2, "land-123");
        }

        @Test
        @DisplayName("Binds current time from TimeProvider")
        void bindsCurrentTime_fromTimeProvider() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            TimeProvider timeProvider = mcRPG.getTimeProvider();
            Instant fixedInstant = Instant.ofEpochMilli(7777L);
            when(timeProvider.now()).thenReturn(fixedInstant);

            BoardCooldownDAO.listCooldowns(mockConnection, "player", "player-uuid");

            verify(mockStatement).setLong(3, 7777L);
        }

        @Test
        @DisplayName("Returns empty list on SQLException")
        void returnsEmptyList_onSQLException() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

            List<BoardCooldownDAO.CooldownRecord> result = BoardCooldownDAO.listCooldowns(
                    mockConnection, "player", "uuid");

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("deleteCooldowns")
    class DeleteCooldowns {

        @Test
        @DisplayName("Deletes all cooldowns for scope when categoryKey is null")
        void deletesAll_whenCategoryKeyNull() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(3);

            int result = BoardCooldownDAO.deleteCooldowns(
                    mockConnection, "player", "player-uuid", null);

            assertEquals(3, result);
            verify(mockStatement).setString(1, "player");
            verify(mockStatement).setString(2, "player-uuid");
            verify(mockStatement, never()).setString(eq(3), anyString());
        }

        @Test
        @DisplayName("Filters by categoryKey when non-null")
        void filtersByCategoryKey_whenNonNull() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeUpdate()).thenReturn(1);

            NamespacedKey categoryKey = new NamespacedKey("mcrpg", "daily_shared");
            int result = BoardCooldownDAO.deleteCooldowns(
                    mockConnection, "player", "player-uuid", categoryKey);

            assertEquals(1, result);
            verify(mockStatement).setString(3, "mcrpg:daily_shared");
        }

        @Test
        @DisplayName("Returns zero on SQLException")
        void returnsZero_onSQLException() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

            int result = BoardCooldownDAO.deleteCooldowns(
                    mockConnection, "player", "uuid", null);

            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("pruneExpiredCooldowns")
    class PruneExpiredCooldowns {

        @Test
        @DisplayName("Returns prepared statements")
        void returnsPreparedStatements() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            List<PreparedStatement> statements = BoardCooldownDAO.pruneExpiredCooldowns(mockConnection);

            assertNotNull(statements);
            assertFalse(statements.isEmpty());
        }

        @Test
        @DisplayName("Binds current time from TimeProvider")
        void bindsCurrentTime_fromTimeProvider() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            TimeProvider timeProvider = mcRPG.getTimeProvider();
            Instant fixedInstant = Instant.ofEpochMilli(9999L);
            when(timeProvider.now()).thenReturn(fixedInstant);

            BoardCooldownDAO.pruneExpiredCooldowns(mockConnection);

            verify(mockStatement).setLong(1, 9999L);
        }

        @Test
        @DisplayName("Returns empty list on SQLException")
        void returnsEmptyList_onSQLException() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

            List<PreparedStatement> result = BoardCooldownDAO.pruneExpiredCooldowns(mockConnection);

            assertTrue(result.isEmpty());
        }
    }
}
