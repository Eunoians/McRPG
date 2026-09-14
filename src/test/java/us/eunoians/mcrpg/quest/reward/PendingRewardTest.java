package us.eunoians.mcrpg.quest.reward;

import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("PendingReward")
class PendingRewardTest {

    private static final UUID REWARD_ID = UUID.randomUUID();
    private static final UUID PLAYER_UUID = UUID.randomUUID();

    private PendingReward newReward() {
        return new PendingReward(
                REWARD_ID,
                PLAYER_UUID,
                new NamespacedKey("mcrpg", "experience"),
                Map.of("amount", 500, "skill", "swords"),
                new NamespacedKey("mcrpg", "daily_quest"),
                1000L,
                2000L
        );
    }

    @Nested
    @DisplayName("getters")
    class Getters {

        @Test
        @DisplayName("getId returns the reward identifier")
        void getId_returnsRewardId() {
            PendingReward reward = newReward();
            assertEquals(REWARD_ID, reward.getId());
        }

        @Test
        @DisplayName("getPlayerUUID returns the player UUID")
        void getPlayerUUID_returnsPlayerUUID() {
            PendingReward reward = newReward();
            assertEquals(PLAYER_UUID, reward.getPlayerUUID());
        }

        @Test
        @DisplayName("getRewardTypeKey returns the reward type key")
        void getRewardTypeKey_returnsRewardTypeKey() {
            PendingReward reward = newReward();
            assertEquals(new NamespacedKey("mcrpg", "experience"), reward.getRewardTypeKey());
        }

        @Test
        @DisplayName("getSerializedConfig returns the config map")
        void getSerializedConfig_returnsConfigMap() {
            PendingReward reward = newReward();
            Map<String, Object> config = reward.getSerializedConfig();
            assertEquals(500, config.get("amount"));
            assertEquals("swords", config.get("skill"));
        }

        @Test
        @DisplayName("getQuestKey returns the quest definition key")
        void getQuestKey_returnsQuestKey() {
            PendingReward reward = newReward();
            assertEquals(new NamespacedKey("mcrpg", "daily_quest"), reward.getQuestKey());
        }

        @Test
        @DisplayName("getCreatedAt returns the creation timestamp")
        void getCreatedAt_returnsCreatedAt() {
            PendingReward reward = newReward();
            assertEquals(1000L, reward.getCreatedAt());
        }

        @Test
        @DisplayName("getExpiresAt returns the expiration timestamp")
        void getExpiresAt_returnsExpiresAt() {
            PendingReward reward = newReward();
            assertEquals(2000L, reward.getExpiresAt());
        }
    }

    @Nested
    @DisplayName("serializedConfig defensive copy")
    class SerializedConfigDefensiveCopy {

        @Test
        @DisplayName("mutation of original map does not affect the stored config")
        void constructor_copiesMap_externalMutationHasNoEffect() {
            HashMap<String, Object> mutableConfig = new HashMap<>();
            mutableConfig.put("key", "original");

            PendingReward reward = new PendingReward(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    new NamespacedKey("mcrpg", "test"),
                    mutableConfig,
                    new NamespacedKey("mcrpg", "quest"),
                    0L,
                    0L
            );

            mutableConfig.put("key", "mutated");

            assertEquals("original", reward.getSerializedConfig().get("key"));
        }

        @Test
        @DisplayName("returned config map is unmodifiable")
        void getSerializedConfig_returnsUnmodifiableMap() {
            PendingReward reward = newReward();
            assertThrows(UnsupportedOperationException.class,
                    () -> reward.getSerializedConfig().put("new_key", "value"));
        }
    }

    @Nested
    @DisplayName("edge cases")
    class EdgeCases {

        @Test
        @DisplayName("empty serialized config is valid")
        void emptyConfig_isValid() {
            PendingReward reward = new PendingReward(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    new NamespacedKey("mcrpg", "test"),
                    Map.of(),
                    new NamespacedKey("mcrpg", "quest"),
                    0L,
                    0L
            );
            assertTrue(reward.getSerializedConfig().isEmpty());
        }

        @Test
        @DisplayName("createdAt equal to expiresAt is valid")
        void sameCreatedAndExpired_isValid() {
            PendingReward reward = new PendingReward(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    new NamespacedKey("mcrpg", "test"),
                    Map.of(),
                    new NamespacedKey("mcrpg", "quest"),
                    5000L,
                    5000L
            );
            assertEquals(5000L, reward.getCreatedAt());
            assertEquals(5000L, reward.getExpiresAt());
        }
    }
}
