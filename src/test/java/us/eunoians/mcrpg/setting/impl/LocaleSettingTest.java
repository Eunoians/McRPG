package us.eunoians.mcrpg.setting.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import us.eunoians.mcrpg.McRPGBaseTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class LocaleSettingTest extends McRPGBaseTest {

    @Nested
    @DisplayName("getSettingKey")
    class GetSettingKey {

        @DisplayName("getSettingKey returns the locale setting key")
        @Test
        void getSettingKey_returnsLocaleKey() {
            assertEquals("mcrpg:locale-setting",
                    LocaleSetting.CLIENT_LOCALE.getSettingKey().toString());
        }

        @DisplayName("All variants share the same setting key")
        @Test
        void allVariants_shareSameKey() {
            assertEquals(LocaleSetting.CLIENT_LOCALE.getSettingKey(),
                    LocaleSetting.SERVER_LOCALE.getSettingKey());
        }
    }

    @Nested
    @DisplayName("fromString")
    class FromString {

        @DisplayName("fromString matches CLIENT_LOCALE")
        @Test
        void fromString_matchesClientLocale() {
            assertEquals(LocaleSetting.CLIENT_LOCALE,
                    LocaleSetting.CLIENT_LOCALE.fromString("CLIENT_LOCALE").orElseThrow());
        }

        @DisplayName("fromString matches SERVER_LOCALE")
        @Test
        void fromString_matchesServerLocale() {
            assertEquals(LocaleSetting.SERVER_LOCALE,
                    LocaleSetting.CLIENT_LOCALE.fromString("SERVER_LOCALE").orElseThrow());
        }

        @DisplayName("fromString is case-insensitive for CLIENT_LOCALE")
        @Test
        void fromString_caseInsensitive_clientLocale() {
            assertEquals(LocaleSetting.CLIENT_LOCALE,
                    LocaleSetting.CLIENT_LOCALE.fromString("client_locale").orElseThrow());
        }

        @DisplayName("fromString is case-insensitive for SERVER_LOCALE")
        @Test
        void fromString_caseInsensitive_serverLocale() {
            assertEquals(LocaleSetting.SERVER_LOCALE,
                    LocaleSetting.CLIENT_LOCALE.fromString("server_locale").orElseThrow());
        }

        @DisplayName("fromString handles mixed case")
        @Test
        void fromString_mixedCase() {
            assertEquals(LocaleSetting.CLIENT_LOCALE,
                    LocaleSetting.CLIENT_LOCALE.fromString("Client_Locale").orElseThrow());
        }

        @DisplayName("fromString returns empty for unknown value")
        @Test
        void fromString_unknownValue_returnsEmpty() {
            assertTrue(LocaleSetting.CLIENT_LOCALE.fromString("NOT_A_SETTING").isEmpty());
        }

        @DisplayName("fromString returns empty for empty string")
        @Test
        void fromString_emptyString_returnsEmpty() {
            assertTrue(LocaleSetting.CLIENT_LOCALE.fromString("").isEmpty());
        }

        @DisplayName("fromString works from any variant instance")
        @ParameterizedTest
        @EnumSource(LocaleSetting.class)
        void fromString_worksFromAnyInstance(LocaleSetting setting) {
            assertEquals(LocaleSetting.CLIENT_LOCALE,
                    setting.fromString("CLIENT_LOCALE").orElseThrow());
        }

        @DisplayName("fromString recognizes available locale codes as SpecificLocaleSetting")
        @Test
        void fromString_recognizesLocaleCode() {
            var availableCodes = SpecificLocaleSetting.getAvailableLocaleCodes();
            assumeFalse(availableCodes.isEmpty(),
                    "No locale codes available in test profile — skipping");
            String code = availableCodes.getFirst();
            var result = LocaleSetting.CLIENT_LOCALE.fromString(code);
            assertTrue(result.isPresent(), "fromString should recognize locale code: " + code);
            assertTrue(result.get() instanceof SpecificLocaleSetting,
                    "Locale code should resolve to SpecificLocaleSetting");
        }
    }

    @Nested
    @DisplayName("getNextSetting")
    class GetNextSetting {

        @DisplayName("CLIENT_LOCALE next setting is SERVER_LOCALE")
        @Test
        void getNextSetting_clientLocale_cyclesToServerLocale() {
            var next = LocaleSetting.CLIENT_LOCALE.getNextSetting().getNodeValue();
            assertEquals(LocaleSetting.SERVER_LOCALE, next);
        }
    }

    @Nested
    @DisplayName("Enum basics")
    class EnumBasics {

        @DisplayName("valueOf resolves CLIENT_LOCALE")
        @Test
        void valueOf_clientLocale() {
            assertEquals(LocaleSetting.CLIENT_LOCALE, LocaleSetting.valueOf("CLIENT_LOCALE"));
        }

        @DisplayName("valueOf resolves SERVER_LOCALE")
        @Test
        void valueOf_serverLocale() {
            assertEquals(LocaleSetting.SERVER_LOCALE, LocaleSetting.valueOf("SERVER_LOCALE"));
        }

        @DisplayName("values() contains exactly two variants")
        @Test
        void values_containsTwoVariants() {
            assertEquals(2, LocaleSetting.values().length);
        }

        @DisplayName("toString matches name for all variants")
        @ParameterizedTest
        @EnumSource(LocaleSetting.class)
        void toString_matchesName(LocaleSetting setting) {
            assertEquals(setting.name(), setting.toString());
        }

        @DisplayName("getExpansionKey returns the McRPG expansion key")
        @Test
        void getExpansionKey_returnsMcRPGExpansionKey() {
            assertFalse(LocaleSetting.CLIENT_LOCALE.getExpansionKey().isEmpty());
            assertEquals("mcrpg:mcrpg-expansion",
                    LocaleSetting.CLIENT_LOCALE.getExpansionKey().orElseThrow().toString());
        }
    }
}
