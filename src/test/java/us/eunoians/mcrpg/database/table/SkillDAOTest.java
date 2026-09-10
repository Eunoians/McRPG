package us.eunoians.mcrpg.database.table;

import com.diamonddagger590.mccore.database.Database;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.ability.AbilityData;
import us.eunoians.mcrpg.ability.attribute.AbilityAttribute;
import us.eunoians.mcrpg.ability.attribute.OptionalSavingAbilityAttribute;
import us.eunoians.mcrpg.entity.holder.SkillHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class SkillDAOTest extends McRPGBaseTest {

    private static final NamespacedKey SKILL_KEY = new NamespacedKey("mcrpg", "swords");
    private static final UUID PLAYER_UUID = UUID.randomUUID();

    @Nested
    @DisplayName("attemptCreateTable")
    class AttemptCreateTable {

        @Test
        @DisplayName("Returns false when both tables exist")
        void returnsFalse_whenBothTablesExist() {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_skill_data")).thenReturn(true);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_ability_attributes")).thenReturn(true);

            boolean result = SkillDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertFalse(result);
        }

        @Test
        @DisplayName("Returns true when skill data table is missing")
        void returnsTrue_whenSkillDataTableMissing() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_skill_data")).thenReturn(false);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_ability_attributes")).thenReturn(true);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = SkillDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
        }

        @Test
        @DisplayName("Returns true when ability attribute table is missing")
        void returnsTrue_whenAbilityAttributeTableMissing() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_skill_data")).thenReturn(true);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_ability_attributes")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = SkillDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
        }

        @Test
        @DisplayName("Returns true when both tables are missing")
        void returnsTrue_whenBothTablesMissing() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            Database mockDatabase = mock(Database.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_skill_data")).thenReturn(false);
            when(mockDatabase.tableExists(mockConnection, "mcrpg_ability_attributes")).thenReturn(false);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            boolean result = SkillDAO.attemptCreateTable(mockConnection, mockDatabase);

            assertTrue(result);
        }
    }

    @Nested
    @DisplayName("getPlayerSkillLevelingData")
    class GetPlayerSkillLevelingData {

        @Test
        @DisplayName("Returns zero experience when no rows")
        void returnsZeroExperience_whenNoRows() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
            SkillDAO.getPlayerSkillLevelingData(mockConnection, PLAYER_UUID, snapshot);

            assertEquals(0, snapshot.getTotalExperience());
        }

        @Test
        @DisplayName("Returns experience when row exists")
        void returnsExperience_whenRowExists() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("total_experience")).thenReturn(500);

            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
            SkillDAO.getPlayerSkillLevelingData(mockConnection, PLAYER_UUID, snapshot);

            assertEquals(500, snapshot.getTotalExperience());
        }

        @Test
        @DisplayName("Binds correct parameters")
        void bindsCorrectParameters() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
            SkillDAO.getPlayerSkillLevelingData(mockConnection, PLAYER_UUID, snapshot);

            verify(mockStatement).setString(1, PLAYER_UUID.toString());
            verify(mockStatement).setString(2, "swords");
        }

        @Test
        @DisplayName("Two-arg overload creates snapshot and returns it")
        void twoArgOverload_createsSnapshot() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            ResultSet mockResultSet = mock(ResultSet.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
            when(mockStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true, false);
            when(mockResultSet.getInt("total_experience")).thenReturn(750);

            SkillDataSnapshot result = SkillDAO.getPlayerSkillLevelingData(mockConnection, PLAYER_UUID, SKILL_KEY);

            assertEquals(PLAYER_UUID, result.getUUID());
            assertEquals(SKILL_KEY, result.getSkillKey());
            assertEquals(750, result.getTotalExperience());
        }

        @Test
        @DisplayName("Returns snapshot unchanged on SQLException")
        void returnsSnapshotUnchanged_onSQLException() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

            SkillDataSnapshot snapshot = new SkillDataSnapshot(PLAYER_UUID, SKILL_KEY);
            SkillDataSnapshot result = SkillDAO.getPlayerSkillLevelingData(mockConnection, PLAYER_UUID, snapshot);

            assertSame(snapshot, result);
            assertEquals(0, result.getTotalExperience());
        }
    }

    @Nested
    @DisplayName("savePlayerSkillData (single skill)")
    class SavePlayerSkillData {

        @Test
        @DisplayName("Produces REPLACE statement when experience is non-zero")
        void producesReplaceStatement_whenExperienceNonZero() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData mockData = mock(SkillHolder.SkillHolderData.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.of(mockData));
            when(mockData.getTotalExperience()).thenReturn(100);

            List<PreparedStatement> result = SkillDAO.savePlayerSkillData(mockConnection, mockSkillHolder, SKILL_KEY);

            assertEquals(1, result.size());
            assertEquals(mockReplaceStatement, result.get(0));
        }

        @Test
        @DisplayName("Produces DELETE statement when experience is zero")
        void producesDeleteStatement_whenExperienceZero() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData mockData = mock(SkillHolder.SkillHolderData.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.of(mockData));
            when(mockData.getTotalExperience()).thenReturn(0);

            List<PreparedStatement> result = SkillDAO.savePlayerSkillData(mockConnection, mockSkillHolder, SKILL_KEY);

            assertEquals(1, result.size());
            assertEquals(mockDeleteStatement, result.get(0));
        }

        @Test
        @DisplayName("Produces empty list when no SkillHolderData")
        void producesEmptyList_whenNoSkillHolderData() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.empty());

            List<PreparedStatement> result = SkillDAO.savePlayerSkillData(mockConnection, mockSkillHolder, SKILL_KEY);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Binds player UUID and skill key to REPLACE statement")
        void bindsParametersToReplace() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData mockData = mock(SkillHolder.SkillHolderData.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.of(mockData));
            when(mockData.getTotalExperience()).thenReturn(250);

            SkillDAO.savePlayerSkillData(mockConnection, mockSkillHolder, SKILL_KEY);

            verify(mockReplaceStatement).setString(1, PLAYER_UUID.toString());
            verify(mockReplaceStatement).setString(2, SKILL_KEY.value());
            verify(mockReplaceStatement).setInt(3, 250);
        }

        @Test
        @DisplayName("Binds player UUID and skill key to DELETE statement")
        void bindsParametersToDelete() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            SkillHolder.SkillHolderData mockData = mock(SkillHolder.SkillHolderData.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getSkillHolderData(SKILL_KEY)).thenReturn(Optional.of(mockData));
            when(mockData.getTotalExperience()).thenReturn(0);

            SkillDAO.savePlayerSkillData(mockConnection, mockSkillHolder, SKILL_KEY);

            verify(mockDeleteStatement).setString(1, PLAYER_UUID.toString());
            verify(mockDeleteStatement).setString(2, SKILL_KEY.value());
        }
    }

    @Nested
    @DisplayName("savePlayerAbilityAttributes (with explicit ability keys)")
    class SavePlayerAbilityAttributes {

        @Test
        @DisplayName("Returns empty list when ability data is absent")
        void returnsEmptyList_whenAbilityDataAbsent() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);

            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "test_ability");
            when(mockSkillHolder.getAbilityData(abilityKey)).thenReturn(Optional.empty());

            List<PreparedStatement> result = SkillDAO.savePlayerAbilityAttributes(mockConnection, mockSkillHolder, Set.of(abilityKey));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Produces REPLACE statement for normal attribute")
        void producesReplace_forNormalAttribute() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "test_ability");
            NamespacedKey attributeKey = new NamespacedKey("mcrpg", "test_attr");

            AbilityAttribute<?> mockAttribute = mock(AbilityAttribute.class);
            when(mockAttribute.getDatabaseKeyName()).thenReturn("test_attr");
            when(mockAttribute.serializeContent()).thenReturn("test_value");

            AbilityData mockAbilityData = mock(AbilityData.class);
            when(mockAbilityData.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbilityData.getAllAttributeKeys()).thenReturn(Set.of(attributeKey));
            when(mockAbilityData.getAbilityAttribute(attributeKey)).thenReturn(Optional.of(mockAttribute));

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getAbilityData(abilityKey)).thenReturn(Optional.of(mockAbilityData));

            List<PreparedStatement> result = SkillDAO.savePlayerAbilityAttributes(mockConnection, mockSkillHolder, Set.of(abilityKey));

            assertEquals(1, result.size());
            assertEquals(mockReplaceStatement, result.get(0));
            verify(mockReplaceStatement).setString(4, "test_value");
        }

        @Test
        @DisplayName("Produces DELETE statement for OptionalSavingAbilityAttribute that should not be saved")
        void producesDelete_forOptionalAttributeNotSaved() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "test_ability");
            NamespacedKey attributeKey = new NamespacedKey("mcrpg", "optional_attr");

            OptionalSavingAbilityAttribute<?> mockOptionalAttribute = mock(OptionalSavingAbilityAttribute.class);
            when(mockOptionalAttribute.shouldContentBeSaved()).thenReturn(false);
            when(mockOptionalAttribute.getDatabaseKeyName()).thenReturn("optional_attr");

            AbilityData mockAbilityData = mock(AbilityData.class);
            when(mockAbilityData.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbilityData.getAllAttributeKeys()).thenReturn(Set.of(attributeKey));
            when(mockAbilityData.getAbilityAttribute(attributeKey)).thenReturn(Optional.of(mockOptionalAttribute));

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getAbilityData(abilityKey)).thenReturn(Optional.of(mockAbilityData));

            List<PreparedStatement> result = SkillDAO.savePlayerAbilityAttributes(mockConnection, mockSkillHolder, Set.of(abilityKey));

            assertEquals(1, result.size());
            assertEquals(mockDeleteStatement, result.get(0));
        }

        @Test
        @DisplayName("Produces REPLACE statement for OptionalSavingAbilityAttribute that should be saved")
        void producesReplace_forOptionalAttributeSaved() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "test_ability");
            NamespacedKey attributeKey = new NamespacedKey("mcrpg", "optional_attr");

            OptionalSavingAbilityAttribute<?> mockOptionalAttribute = mock(OptionalSavingAbilityAttribute.class);
            when(mockOptionalAttribute.shouldContentBeSaved()).thenReturn(true);
            when(mockOptionalAttribute.getDatabaseKeyName()).thenReturn("optional_attr");
            when(mockOptionalAttribute.serializeContent()).thenReturn("saved_value");

            AbilityData mockAbilityData = mock(AbilityData.class);
            when(mockAbilityData.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbilityData.getAllAttributeKeys()).thenReturn(Set.of(attributeKey));
            when(mockAbilityData.getAbilityAttribute(attributeKey)).thenReturn(Optional.of(mockOptionalAttribute));

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getAbilityData(abilityKey)).thenReturn(Optional.of(mockAbilityData));

            List<PreparedStatement> result = SkillDAO.savePlayerAbilityAttributes(mockConnection, mockSkillHolder, Set.of(abilityKey));

            assertEquals(1, result.size());
            assertEquals(mockReplaceStatement, result.get(0));
            verify(mockReplaceStatement).setString(4, "saved_value");
        }

        @Test
        @DisplayName("Skips attributes not present in ability data")
        void skipsAttributes_whenNotPresentInAbilityData() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "test_ability");
            NamespacedKey attributeKey = new NamespacedKey("mcrpg", "missing_attr");

            AbilityData mockAbilityData = mock(AbilityData.class);
            when(mockAbilityData.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbilityData.getAllAttributeKeys()).thenReturn(Set.of(attributeKey));
            when(mockAbilityData.getAbilityAttribute(attributeKey)).thenReturn(Optional.empty());

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getAbilityData(abilityKey)).thenReturn(Optional.of(mockAbilityData));

            List<PreparedStatement> result = SkillDAO.savePlayerAbilityAttributes(mockConnection, mockSkillHolder, Set.of(abilityKey));

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Handles multiple abilities with multiple attributes")
        void handlesMultipleAbilitiesWithMultipleAttributes() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            PreparedStatement mockReplaceStatement = mock(PreparedStatement.class);
            PreparedStatement mockDeleteStatement = mock(PreparedStatement.class);
            when(mockConnection.prepareStatement(contains("REPLACE"))).thenReturn(mockReplaceStatement);
            when(mockConnection.prepareStatement(contains("DELETE"))).thenReturn(mockDeleteStatement);

            NamespacedKey ability1Key = new NamespacedKey("mcrpg", "ability_1");
            NamespacedKey ability2Key = new NamespacedKey("mcrpg", "ability_2");
            NamespacedKey attr1Key = new NamespacedKey("mcrpg", "attr_1");
            NamespacedKey attr2Key = new NamespacedKey("mcrpg", "attr_2");

            AbilityAttribute<?> mockAttr1 = mock(AbilityAttribute.class);
            when(mockAttr1.getDatabaseKeyName()).thenReturn("attr_1");
            when(mockAttr1.serializeContent()).thenReturn("val_1");

            AbilityAttribute<?> mockAttr2 = mock(AbilityAttribute.class);
            when(mockAttr2.getDatabaseKeyName()).thenReturn("attr_2");
            when(mockAttr2.serializeContent()).thenReturn("val_2");

            AbilityData mockData1 = mock(AbilityData.class);
            when(mockData1.getAbilityKey()).thenReturn(ability1Key);
            when(mockData1.getAllAttributeKeys()).thenReturn(Set.of(attr1Key));
            when(mockData1.getAbilityAttribute(attr1Key)).thenReturn(Optional.of(mockAttr1));

            AbilityData mockData2 = mock(AbilityData.class);
            when(mockData2.getAbilityKey()).thenReturn(ability2Key);
            when(mockData2.getAllAttributeKeys()).thenReturn(Set.of(attr2Key));
            when(mockData2.getAbilityAttribute(attr2Key)).thenReturn(Optional.of(mockAttr2));

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getAbilityData(ability1Key)).thenReturn(Optional.of(mockData1));
            when(mockSkillHolder.getAbilityData(ability2Key)).thenReturn(Optional.of(mockData2));

            List<PreparedStatement> result = SkillDAO.savePlayerAbilityAttributes(
                    mockConnection, mockSkillHolder, Set.of(ability1Key, ability2Key));

            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Returns empty list on SQLException")
        void returnsEmptyList_onSQLException() throws SQLException {
            Connection mockConnection = mock(Connection.class);
            when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("fail"));

            NamespacedKey abilityKey = new NamespacedKey("mcrpg", "test_ability");
            NamespacedKey attributeKey = new NamespacedKey("mcrpg", "test_attr");

            AbilityAttribute<?> mockAttribute = mock(AbilityAttribute.class);
            when(mockAttribute.getDatabaseKeyName()).thenReturn("test_attr");

            AbilityData mockAbilityData = mock(AbilityData.class);
            when(mockAbilityData.getAbilityKey()).thenReturn(abilityKey);
            when(mockAbilityData.getAllAttributeKeys()).thenReturn(Set.of(attributeKey));
            when(mockAbilityData.getAbilityAttribute(attributeKey)).thenReturn(Optional.of(mockAttribute));

            SkillHolder mockSkillHolder = mock(SkillHolder.class);
            when(mockSkillHolder.getUUID()).thenReturn(PLAYER_UUID);
            when(mockSkillHolder.getAbilityData(abilityKey)).thenReturn(Optional.of(mockAbilityData));

            List<PreparedStatement> result = SkillDAO.savePlayerAbilityAttributes(
                    mockConnection, mockSkillHolder, Set.of(abilityKey));

            assertTrue(result.isEmpty());
        }
    }
}
