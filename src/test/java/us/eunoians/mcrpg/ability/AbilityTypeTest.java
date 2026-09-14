package us.eunoians.mcrpg.ability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("AbilityType")
class AbilityTypeTest {

    @Nested
    @DisplayName("fromString")
    class FromString {

        @ParameterizedTest
        @EnumSource(AbilityType.class)
        @DisplayName("parses uppercase constant names")
        void fromString_uppercaseConstantName_returnsMatchingType(AbilityType type) {
            Optional<AbilityType> result = AbilityType.fromString(type.name());
            assertTrue(result.isPresent());
            assertEquals(type, result.get());
        }

        @ParameterizedTest
        @EnumSource(AbilityType.class)
        @DisplayName("parses lowercase constant names case-insensitively")
        void fromString_lowercaseName_returnsMatchingType(AbilityType type) {
            Optional<AbilityType> result = AbilityType.fromString(type.name().toLowerCase());
            assertTrue(result.isPresent());
            assertEquals(type, result.get());
        }

        @ParameterizedTest
        @EnumSource(AbilityType.class)
        @DisplayName("parses mixed-case constant names case-insensitively")
        void fromString_mixedCaseName_returnsMatchingType(AbilityType type) {
            String mixedCase = type.name().charAt(0) + type.name().substring(1).toLowerCase();
            Optional<AbilityType> result = AbilityType.fromString(mixedCase);
            assertTrue(result.isPresent());
            assertEquals(type, result.get());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("returns empty for null, empty, and blank strings")
        void fromString_nullEmptyOrBlank_returnsEmpty(String value) {
            assertTrue(AbilityType.fromString(value).isEmpty());
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "unknown", "foo", "ACTIVE_PASSIVE", "activee"})
        @DisplayName("returns empty for unrecognized values")
        void fromString_unrecognizedValue_returnsEmpty(String value) {
            assertTrue(AbilityType.fromString(value).isEmpty());
        }
    }

    @Test
    @DisplayName("valueOf round-trips all expected constants")
    void valueOf_roundTripsExpectedConstants() {
        assertEquals(AbilityType.ACTIVE, AbilityType.valueOf("ACTIVE"));
        assertEquals(AbilityType.PASSIVE, AbilityType.valueOf("PASSIVE"));
        assertEquals(AbilityType.INNATE, AbilityType.valueOf("INNATE"));
    }

    @Test
    @DisplayName("enum has exactly three constants")
    void enum_hasExactlyThreeConstants() {
        assertEquals(3, AbilityType.values().length);
    }
}
