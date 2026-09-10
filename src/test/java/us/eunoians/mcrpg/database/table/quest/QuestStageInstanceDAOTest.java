package us.eunoians.mcrpg.database.table.quest;

import com.diamonddagger590.mccore.database.Database;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.quest.QuestTestHelper;
import us.eunoians.mcrpg.quest.definition.QuestDefinition;
import us.eunoians.mcrpg.quest.impl.QuestInstance;
import us.eunoians.mcrpg.quest.impl.stage.QuestStageInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class QuestStageInstanceDAOTest extends McRPGBaseTest {

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("Returns false when table already exists")
        void returnsFalse_whenTableExists() {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            when(db.tableExists(conn, QuestStageInstanceDAO.TABLE_NAME)).thenReturn(true);

            boolean result = QuestStageInstanceDAO.attemptCreateTable(conn, db);

            assertFalse(result);
        }

        @Test
        @DisplayName("Returns true when table does not exist and is created")
        void returnsTrue_whenTableCreated() throws SQLException {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(db.tableExists(conn, QuestStageInstanceDAO.TABLE_NAME)).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            boolean result = QuestStageInstanceDAO.attemptCreateTable(conn, db);

            assertTrue(result);
            verify(ps).executeUpdate();
        }

        @Test
        @DisplayName("Returns false on SQLException during creation")
        void returnsFalse_onSQLException() throws SQLException {
            Connection conn = mock(Connection.class);
            Database db = mock(Database.class);
            when(db.tableExists(conn, QuestStageInstanceDAO.TABLE_NAME)).thenReturn(false);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("creation failed"));

            boolean result = QuestStageInstanceDAO.attemptCreateTable(conn, db);

            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("saveStageInstance")
    class SaveStageInstance {

        @Test
        @DisplayName("Returns prepared statements for a valid stage")
        void returnsPreparedStatements() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_save");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);

            List<PreparedStatement> result = QuestStageInstanceDAO.saveStageInstance(conn, stage);

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Calls prepareStatement on the connection")
        void callsPrepareStatement() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement mockPs = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(mockPs);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_ps");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);

            QuestStageInstanceDAO.saveStageInstance(conn, stage);

            verify(conn).prepareStatement(anyString());
        }

        @Test
        @DisplayName("Returns empty list on SQLException")
        void returnsEmptyList_onSQLException() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_err");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            QuestStageInstance stage = quest.getQuestStageInstances().get(0);

            List<PreparedStatement> result = QuestStageInstanceDAO.saveStageInstance(conn, stage);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("saveAllStageInstances")
    class SaveAllStageInstances {

        @Test
        @DisplayName("Returns prepared statements for all stages")
        void returnsPreparedStatements() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_all_stages");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            List<PreparedStatement> result = QuestStageInstanceDAO.saveAllStageInstances(conn, quest);

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Statement count matches stage count")
        void statementCountMatchesStageCount() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_stage_count");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            int stageCount = quest.getQuestStageInstances().size();

            List<PreparedStatement> result = QuestStageInstanceDAO.saveAllStageInstances(conn, quest);

            assertEquals(stageCount, result.size());
        }
    }

    @Nested
    @DisplayName("loadStageInstances")
    class LoadStageInstances {

        @Test
        @DisplayName("Returns empty list when no rows found")
        void returnsEmptyList_whenNoRows() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_empty");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            List<QuestStageInstance> result = QuestStageInstanceDAO.loadStageInstances(conn, quest.getQuestUUID(), quest);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Loads stage from valid row")
        void loadsStage_fromValidRow() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);

            UUID stageUUID = UUID.randomUUID();
            when(rs.getString("stage_uuid")).thenReturn(stageUUID.toString());
            when(rs.getString("definition_key")).thenReturn("mcrpg:test_stage");
            when(rs.getInt("phase_index")).thenReturn(0);
            when(rs.getString("state")).thenReturn("IN_PROGRESS");
            when(rs.getLong("start_time")).thenReturn(1000L);
            when(rs.wasNull()).thenReturn(false, true);
            when(rs.getLong("end_time")).thenReturn(0L);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_valid");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            List<QuestStageInstance> result = QuestStageInstanceDAO.loadStageInstances(conn, quest.getQuestUUID(), quest);

            assertEquals(1, result.size());
            QuestStageInstance loaded = result.get(0);
            assertEquals(stageUUID, loaded.getQuestStageUUID());
            assertEquals("mcrpg:test_stage", loaded.getStageKey().toString());
            assertEquals(0, loaded.getPhaseIndex());
        }

        @Test
        @DisplayName("Skips row with malformed definition key")
        void skipsRow_withMalformedDefinitionKey() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, true, false);

            when(rs.getString("stage_uuid")).thenReturn(UUID.randomUUID().toString(), UUID.randomUUID().toString());
            when(rs.getString("definition_key")).thenReturn("!!!invalid!!!", "mcrpg:valid_stage");
            when(rs.getInt("phase_index")).thenReturn(0, 0);
            when(rs.getString("state")).thenReturn("NOT_STARTED", "NOT_STARTED");
            when(rs.getLong("start_time")).thenReturn(0L, 0L);
            when(rs.getLong("end_time")).thenReturn(0L, 0L);
            when(rs.wasNull()).thenReturn(true);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_malformed");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            List<QuestStageInstance> result = QuestStageInstanceDAO.loadStageInstances(conn, quest.getQuestUUID(), quest);

            assertEquals(1, result.size());
            assertEquals("mcrpg:valid_stage", result.get(0).getStageKey().toString());
        }

        @Test
        @DisplayName("Handles nullable start and end times")
        void handlesNullableTimestamps() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);

            when(rs.getString("stage_uuid")).thenReturn(UUID.randomUUID().toString());
            when(rs.getString("definition_key")).thenReturn("mcrpg:null_time_stage");
            when(rs.getInt("phase_index")).thenReturn(0);
            when(rs.getString("state")).thenReturn("NOT_STARTED");
            when(rs.getLong("start_time")).thenReturn(0L);
            when(rs.getLong("end_time")).thenReturn(0L);
            when(rs.wasNull()).thenReturn(true);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_null_time");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            List<QuestStageInstance> result = QuestStageInstanceDAO.loadStageInstances(conn, quest.getQuestUUID(), quest);

            assertEquals(1, result.size());
            assertTrue(result.get(0).getStartTime().isEmpty());
            assertTrue(result.get(0).getEndTime().isEmpty());
        }

        @Test
        @DisplayName("Returns empty list on SQLException")
        void returnsEmptyList_onSQLException() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("load failed"));

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_error");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);

            List<QuestStageInstance> result = QuestStageInstanceDAO.loadStageInstances(conn, quest.getQuestUUID(), quest);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Binds quest UUID as parameter")
        void bindsQuestUUID() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            QuestDefinition def = QuestTestHelper.singlePhaseQuest("dao_load_bind");
            QuestInstance quest = QuestTestHelper.startedQuestInstance(def);
            UUID questUUID = quest.getQuestUUID();

            QuestStageInstanceDAO.loadStageInstances(conn, questUUID, quest);

            verify(ps).setString(1, questUUID.toString());
        }
    }

    @Nested
    @DisplayName("deleteStageInstances")
    class DeleteStageInstances {

        @Test
        @DisplayName("Returns prepared statements")
        void returnsPreparedStatements() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenReturn(mock(PreparedStatement.class));

            List<PreparedStatement> result = QuestStageInstanceDAO.deleteStageInstances(conn, UUID.randomUUID());

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Binds quest UUID as parameter")
        void bindsQuestUUID() throws SQLException {
            Connection conn = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            when(conn.prepareStatement(anyString())).thenReturn(ps);

            UUID questUUID = UUID.randomUUID();
            QuestStageInstanceDAO.deleteStageInstances(conn, questUUID);

            verify(ps).setString(1, questUUID.toString());
        }

        @Test
        @DisplayName("Returns empty list on SQLException")
        void returnsEmptyList_onSQLException() throws SQLException {
            Connection conn = mock(Connection.class);
            when(conn.prepareStatement(anyString())).thenThrow(new SQLException("delete failed"));

            List<PreparedStatement> result = QuestStageInstanceDAO.deleteStageInstances(conn, UUID.randomUUID());

            assertTrue(result.isEmpty());
        }
    }
}
