package us.eunoians.mcrpg.skill.component;

import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

@DisplayName("EventLevelableComponentAttribute")
class EventLevelableComponentAttributeTest {

    @Nested
    @DisplayName("constructor and accessors")
    class ConstructorAndAccessors {

        @DisplayName("stores and returns the levelable component")
        @Test
        void levelableComponent_returnsStoredComponent() {
            EventLevelableComponent component = mock(EventLevelableComponent.class);
            var attribute = new EventLevelableComponentAttribute(component, BlockBreakEvent.class, 0);
            assertSame(component, attribute.levelableComponent());
        }

        @DisplayName("stores and returns the event class")
        @Test
        void clazz_returnsStoredEventClass() {
            EventLevelableComponent component = mock(EventLevelableComponent.class);
            var attribute = new EventLevelableComponentAttribute(component, EntityDamageByEntityEvent.class, 1);
            assertEquals(EntityDamageByEntityEvent.class, attribute.clazz());
        }

        @DisplayName("stores and returns the priority")
        @Test
        void priority_returnsStoredPriority() {
            EventLevelableComponent component = mock(EventLevelableComponent.class);
            var attribute = new EventLevelableComponentAttribute(component, Event.class, 42);
            assertEquals(42, attribute.priority());
        }
    }

    @Nested
    @DisplayName("equality")
    class Equality {

        @DisplayName("equals returns true for identical attributes")
        @Test
        void equals_returnsTrue_whenIdentical() {
            EventLevelableComponent component = mock(EventLevelableComponent.class);
            var attr1 = new EventLevelableComponentAttribute(component, BlockBreakEvent.class, 5);
            var attr2 = new EventLevelableComponentAttribute(component, BlockBreakEvent.class, 5);
            assertEquals(attr1, attr2);
        }

        @DisplayName("equals returns false when priority differs")
        @Test
        void equals_returnsFalse_whenPriorityDiffers() {
            EventLevelableComponent component = mock(EventLevelableComponent.class);
            var attr1 = new EventLevelableComponentAttribute(component, BlockBreakEvent.class, 0);
            var attr2 = new EventLevelableComponentAttribute(component, BlockBreakEvent.class, 1);
            assertNotEquals(attr1, attr2);
        }

        @DisplayName("equals returns false when event class differs")
        @Test
        void equals_returnsFalse_whenEventClassDiffers() {
            EventLevelableComponent component = mock(EventLevelableComponent.class);
            var attr1 = new EventLevelableComponentAttribute(component, BlockBreakEvent.class, 0);
            var attr2 = new EventLevelableComponentAttribute(component, EntityDamageByEntityEvent.class, 0);
            assertNotEquals(attr1, attr2);
        }

        @DisplayName("hashCode is consistent for equal attributes")
        @Test
        void hashCode_isConsistent_whenEqual() {
            EventLevelableComponent component = mock(EventLevelableComponent.class);
            var attr1 = new EventLevelableComponentAttribute(component, BlockBreakEvent.class, 5);
            var attr2 = new EventLevelableComponentAttribute(component, BlockBreakEvent.class, 5);
            assertEquals(attr1.hashCode(), attr2.hashCode());
        }
    }
}
