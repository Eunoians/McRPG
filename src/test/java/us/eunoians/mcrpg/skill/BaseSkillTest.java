package us.eunoians.mcrpg.skill;

import com.diamonddagger590.mccore.parser.Parser;
import com.diamonddagger590.mccore.statistic.Statistic;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import us.eunoians.mcrpg.builder.item.skill.SkillItemBuilder;
import us.eunoians.mcrpg.entity.holder.SkillHolder;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.exception.skill.EventNotRegisteredForLevelingException;
import us.eunoians.mcrpg.skill.component.EventLevelableComponent;
import us.eunoians.mcrpg.skill.component.EventLevelableComponentAttribute;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("BaseSkill")
class BaseSkillTest {

    private static final NamespacedKey TEST_SKILL_KEY = new NamespacedKey("mcrpg", "test_skill");

    private TestSkill skill;

    @BeforeEach
    void setUp() {
        skill = new TestSkill(TEST_SKILL_KEY);
    }

    @Nested
    @DisplayName("getSkillKey")
    class GetSkillKey {

        @DisplayName("returns the key passed to the constructor")
        @Test
        void getSkillKey_returnsConstructorKey() {
            assertEquals(TEST_SKILL_KEY, skill.getSkillKey());
        }

        @DisplayName("returns a different key when constructed with a different key")
        @Test
        void getSkillKey_returnsDifferentKey_whenConstructedWithDifferentKey() {
            NamespacedKey otherKey = new NamespacedKey("mcrpg", "other_skill");
            TestSkill otherSkill = new TestSkill(otherKey);
            assertEquals(otherKey, otherSkill.getSkillKey());
        }
    }

    @Nested
    @DisplayName("canEventLevelSkill")
    class CanEventLevelSkill {

        @DisplayName("returns false when no components are registered")
        @Test
        void canEventLevelSkill_returnsFalse_whenNoComponentsRegistered() {
            Event event = mock(BlockBreakEvent.class);
            assertFalse(skill.canEventLevelSkill(event));
        }

        @DisplayName("returns true when a component is registered for the event type")
        @Test
        void canEventLevelSkill_returnsTrue_whenComponentRegistered() {
            EventLevelableComponent component = mock(EventLevelableComponent.class);
            skill.addLevelableComponent(component, BlockBreakEvent.class, 0);

            Event event = mock(BlockBreakEvent.class);
            assertTrue(skill.canEventLevelSkill(event));
        }

        @DisplayName("returns false for a different event type than what is registered")
        @Test
        void canEventLevelSkill_returnsFalse_whenDifferentEventType() {
            EventLevelableComponent component = mock(EventLevelableComponent.class);
            skill.addLevelableComponent(component, BlockBreakEvent.class, 0);

            Event event = mock(EntityDamageByEntityEvent.class);
            assertFalse(skill.canEventLevelSkill(event));
        }
    }

    @Nested
    @DisplayName("getLevelableComponents")
    class GetLevelableComponents {

        @DisplayName("returns an empty list when no components are registered for the event")
        @Test
        void getLevelableComponents_returnsEmptyList_whenNoComponents() {
            List<EventLevelableComponentAttribute> result = skill.getLevelableComponents(BlockBreakEvent.class);
            assertTrue(result.isEmpty());
        }

        @DisplayName("returns the registered component for the correct event type")
        @Test
        void getLevelableComponents_returnsRegisteredComponent() {
            EventLevelableComponent component = mock(EventLevelableComponent.class);
            skill.addLevelableComponent(component, BlockBreakEvent.class, 0);

            List<EventLevelableComponentAttribute> result = skill.getLevelableComponents(BlockBreakEvent.class);
            assertEquals(1, result.size());
            assertSame(component, result.get(0).levelableComponent());
        }

        @DisplayName("returns an empty list for a different event type than registered")
        @Test
        void getLevelableComponents_returnsEmptyList_whenDifferentEventType() {
            EventLevelableComponent component = mock(EventLevelableComponent.class);
            skill.addLevelableComponent(component, BlockBreakEvent.class, 0);

            List<EventLevelableComponentAttribute> result = skill.getLevelableComponents(EntityDamageByEntityEvent.class);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("addLevelableComponent")
    class AddLevelableComponent {

        @DisplayName("registers a component for the specified event type")
        @Test
        void addLevelableComponent_registersComponent() {
            EventLevelableComponent component = mock(EventLevelableComponent.class);
            skill.addLevelableComponent(component, BlockBreakEvent.class, 0);

            List<EventLevelableComponentAttribute> result = skill.getLevelableComponents(BlockBreakEvent.class);
            assertEquals(1, result.size());
            assertEquals(BlockBreakEvent.class, result.get(0).clazz());
            assertEquals(0, result.get(0).priority());
        }

        @DisplayName("allows multiple components for the same event type")
        @Test
        void addLevelableComponent_allowsMultipleComponents() {
            EventLevelableComponent comp1 = mock(EventLevelableComponent.class);
            EventLevelableComponent comp2 = mock(EventLevelableComponent.class);
            skill.addLevelableComponent(comp1, BlockBreakEvent.class, 0);
            skill.addLevelableComponent(comp2, BlockBreakEvent.class, 1);

            List<EventLevelableComponentAttribute> result = skill.getLevelableComponents(BlockBreakEvent.class);
            assertEquals(2, result.size());
        }

        @DisplayName("sorts components by priority after adding")
        @Test
        void addLevelableComponent_sortsByPriority() {
            EventLevelableComponent highPriority = mock(EventLevelableComponent.class);
            EventLevelableComponent lowPriority = mock(EventLevelableComponent.class);

            skill.addLevelableComponent(highPriority, BlockBreakEvent.class, 5);
            skill.addLevelableComponent(lowPriority, BlockBreakEvent.class, 1);

            List<EventLevelableComponentAttribute> result = skill.getLevelableComponents(BlockBreakEvent.class);
            assertSame(lowPriority, result.get(0).levelableComponent());
            assertSame(highPriority, result.get(1).levelableComponent());
        }

        @DisplayName("sorts components across multiple additions correctly")
        @Test
        void addLevelableComponent_sortsMaintainedAcrossAdditions() {
            EventLevelableComponent comp0 = mock(EventLevelableComponent.class);
            EventLevelableComponent comp1 = mock(EventLevelableComponent.class);
            EventLevelableComponent comp2 = mock(EventLevelableComponent.class);

            skill.addLevelableComponent(comp2, BlockBreakEvent.class, 10);
            skill.addLevelableComponent(comp0, BlockBreakEvent.class, 0);
            skill.addLevelableComponent(comp1, BlockBreakEvent.class, 5);

            List<EventLevelableComponentAttribute> result = skill.getLevelableComponents(BlockBreakEvent.class);
            assertEquals(3, result.size());
            assertSame(comp0, result.get(0).levelableComponent());
            assertSame(comp1, result.get(1).levelableComponent());
            assertSame(comp2, result.get(2).levelableComponent());
        }

        @DisplayName("allows components for different event types independently")
        @Test
        void addLevelableComponent_differentEventTypesIndependent() {
            EventLevelableComponent blockComp = mock(EventLevelableComponent.class);
            EventLevelableComponent damageComp = mock(EventLevelableComponent.class);

            skill.addLevelableComponent(blockComp, BlockBreakEvent.class, 0);
            skill.addLevelableComponent(damageComp, EntityDamageByEntityEvent.class, 0);

            assertEquals(1, skill.getLevelableComponents(BlockBreakEvent.class).size());
            assertEquals(1, skill.getLevelableComponents(EntityDamageByEntityEvent.class).size());
            assertSame(blockComp, skill.getLevelableComponents(BlockBreakEvent.class).get(0).levelableComponent());
            assertSame(damageComp, skill.getLevelableComponents(EntityDamageByEntityEvent.class).get(0).levelableComponent());
        }
    }

    @Nested
    @DisplayName("calculateExperienceToGive")
    class CalculateExperienceToGive {

        @DisplayName("throws EventNotRegisteredForLevelingException when event is not registered")
        @Test
        void calculateExperienceToGive_throwsException_whenEventNotRegistered() {
            SkillHolder holder = mock(SkillHolder.class);
            Event event = mock(BlockBreakEvent.class);

            EventNotRegisteredForLevelingException exception = assertThrows(
                    EventNotRegisteredForLevelingException.class,
                    () -> skill.calculateExperienceToGive(holder, event)
            );
            assertSame(event, exception.getFailedEvent());
            assertSame(skill, exception.getSkill());
        }

        @DisplayName("returns 0 when the only component says shouldGiveExperience is false")
        @Test
        void calculateExperienceToGive_returnsZero_whenComponentDeniesExperience() {
            SkillHolder holder = mock(SkillHolder.class);
            BlockBreakEvent event = mock(BlockBreakEvent.class);

            EventLevelableComponent component = mock(EventLevelableComponent.class);
            when(component.shouldGiveExperience(holder, event)).thenReturn(false);
            skill.addLevelableComponent(component, BlockBreakEvent.class, 0);

            assertEquals(0, skill.calculateExperienceToGive(holder, event));
        }

        @DisplayName("returns experience from a single component when it grants experience")
        @Test
        void calculateExperienceToGive_returnsComponentExperience_whenSingleComponent() {
            SkillHolder holder = mock(SkillHolder.class);
            BlockBreakEvent event = mock(BlockBreakEvent.class);

            EventLevelableComponent component = mock(EventLevelableComponent.class);
            when(component.shouldGiveExperience(holder, event)).thenReturn(true);
            when(component.calculateExperienceToGive(holder, event)).thenReturn(25);
            skill.addLevelableComponent(component, BlockBreakEvent.class, 0);

            assertEquals(25, skill.calculateExperienceToGive(holder, event));
        }

        @DisplayName("returns the highest experience when multiple components provide values")
        @Test
        void calculateExperienceToGive_returnsHighest_whenMultipleComponents() {
            SkillHolder holder = mock(SkillHolder.class);
            BlockBreakEvent event = mock(BlockBreakEvent.class);

            EventLevelableComponent comp1 = mock(EventLevelableComponent.class);
            when(comp1.shouldGiveExperience(holder, event)).thenReturn(true);
            when(comp1.calculateExperienceToGive(holder, event)).thenReturn(10);

            EventLevelableComponent comp2 = mock(EventLevelableComponent.class);
            when(comp2.shouldGiveExperience(holder, event)).thenReturn(true);
            when(comp2.calculateExperienceToGive(holder, event)).thenReturn(50);

            EventLevelableComponent comp3 = mock(EventLevelableComponent.class);
            when(comp3.shouldGiveExperience(holder, event)).thenReturn(true);
            when(comp3.calculateExperienceToGive(holder, event)).thenReturn(30);

            skill.addLevelableComponent(comp1, BlockBreakEvent.class, 0);
            skill.addLevelableComponent(comp2, BlockBreakEvent.class, 1);
            skill.addLevelableComponent(comp3, BlockBreakEvent.class, 2);

            assertEquals(50, skill.calculateExperienceToGive(holder, event));
        }

        @DisplayName("returns 0 when a later component denies experience")
        @Test
        void calculateExperienceToGive_returnsZero_whenLaterComponentDenies() {
            SkillHolder holder = mock(SkillHolder.class);
            BlockBreakEvent event = mock(BlockBreakEvent.class);

            EventLevelableComponent comp1 = mock(EventLevelableComponent.class);
            when(comp1.shouldGiveExperience(holder, event)).thenReturn(true);
            when(comp1.calculateExperienceToGive(holder, event)).thenReturn(100);

            EventLevelableComponent comp2 = mock(EventLevelableComponent.class);
            when(comp2.shouldGiveExperience(holder, event)).thenReturn(false);

            skill.addLevelableComponent(comp1, BlockBreakEvent.class, 0);
            skill.addLevelableComponent(comp2, BlockBreakEvent.class, 1);

            assertEquals(0, skill.calculateExperienceToGive(holder, event));
        }

        @DisplayName("returns 0 when the first component denies experience even if later ones would grant it")
        @Test
        void calculateExperienceToGive_returnsZero_whenFirstComponentDenies() {
            SkillHolder holder = mock(SkillHolder.class);
            BlockBreakEvent event = mock(BlockBreakEvent.class);

            EventLevelableComponent comp1 = mock(EventLevelableComponent.class);
            when(comp1.shouldGiveExperience(holder, event)).thenReturn(false);

            EventLevelableComponent comp2 = mock(EventLevelableComponent.class);
            when(comp2.shouldGiveExperience(holder, event)).thenReturn(true);
            when(comp2.calculateExperienceToGive(holder, event)).thenReturn(999);

            skill.addLevelableComponent(comp1, BlockBreakEvent.class, 0);
            skill.addLevelableComponent(comp2, BlockBreakEvent.class, 1);

            assertEquals(0, skill.calculateExperienceToGive(holder, event));
        }

        @DisplayName("returns 0 when all components return 0 experience")
        @Test
        void calculateExperienceToGive_returnsZero_whenAllComponentsReturnZero() {
            SkillHolder holder = mock(SkillHolder.class);
            BlockBreakEvent event = mock(BlockBreakEvent.class);

            EventLevelableComponent comp1 = mock(EventLevelableComponent.class);
            when(comp1.shouldGiveExperience(holder, event)).thenReturn(true);
            when(comp1.calculateExperienceToGive(holder, event)).thenReturn(0);

            EventLevelableComponent comp2 = mock(EventLevelableComponent.class);
            when(comp2.shouldGiveExperience(holder, event)).thenReturn(true);
            when(comp2.calculateExperienceToGive(holder, event)).thenReturn(0);

            skill.addLevelableComponent(comp1, BlockBreakEvent.class, 0);
            skill.addLevelableComponent(comp2, BlockBreakEvent.class, 1);

            assertEquals(0, skill.calculateExperienceToGive(holder, event));
        }

        @DisplayName("respects priority ordering when evaluating components")
        @Test
        void calculateExperienceToGive_respectsPriorityOrder() {
            SkillHolder holder = mock(SkillHolder.class);
            BlockBreakEvent event = mock(BlockBreakEvent.class);

            EventLevelableComponent gatekeeper = mock(EventLevelableComponent.class);
            when(gatekeeper.shouldGiveExperience(holder, event)).thenReturn(false);

            EventLevelableComponent provider = mock(EventLevelableComponent.class);
            when(provider.shouldGiveExperience(holder, event)).thenReturn(true);
            when(provider.calculateExperienceToGive(holder, event)).thenReturn(50);

            skill.addLevelableComponent(provider, BlockBreakEvent.class, 5);
            skill.addLevelableComponent(gatekeeper, BlockBreakEvent.class, 0);

            assertEquals(0, skill.calculateExperienceToGive(holder, event));
        }
    }

    /**
     * Minimal concrete subclass of {@link BaseSkill} for testing purposes.
     */
    private static class TestSkill extends BaseSkill {

        private TestSkill(@NotNull NamespacedKey skillKey) {
            super(skillKey);
        }

        @Override
        public @NotNull Plugin getPlugin() {
            return mock(Plugin.class);
        }

        @Override
        public @NotNull String getDatabaseName() {
            return "test_skill";
        }

        @Override
        public @NotNull String getName(@NotNull McRPGPlayer player) {
            return "Test Skill";
        }

        @Override
        public @NotNull String getName() {
            return "Test Skill";
        }

        @Override
        public @NotNull Component getDisplayName(@NotNull McRPGPlayer player) {
            return Component.text("Test Skill");
        }

        @Override
        public @NotNull Component getDisplayName() {
            return Component.text("Test Skill");
        }

        @Override
        public int getMaxLevel() {
            return 100;
        }

        @Override
        public boolean isSkillEnabled() {
            return true;
        }

        @Override
        public @NotNull SkillItemBuilder getDisplayItemBuilder(@NotNull McRPGPlayer player) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull Parser getLevelUpEquation() {
            throw new UnsupportedOperationException();
        }

        @Override
        public @NotNull Optional<NamespacedKey> getExpansionKey() {
            return Optional.empty();
        }
    }
}
