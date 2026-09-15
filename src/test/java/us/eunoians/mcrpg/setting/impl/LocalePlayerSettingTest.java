package us.eunoians.mcrpg.setting.impl;

import com.diamonddagger590.mccore.gui.Gui;
import com.diamonddagger590.mccore.player.CorePlayer;
import com.diamonddagger590.mccore.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import us.eunoians.mcrpg.McRPGBaseTest;
import us.eunoians.mcrpg.entity.player.McRPGPlayer;
import us.eunoians.mcrpg.gui.setting.PlayerSettingGui;
import us.eunoians.mcrpg.registry.manager.McRPGManagerKey;
import us.eunoians.mcrpg.entity.player.McRPGPlayerExtension;
import us.eunoians.mcrpg.util.McRPGMethods;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

@ExtendWith(McRPGPlayerExtension.class)
class LocalePlayerSettingTest extends McRPGBaseTest {

    @Nested
    @DisplayName("getSettingKey")
    class GetSettingKey {

        @DisplayName("SETTING_KEY has the correct namespace and key")
        @Test
        void settingKey_hasCorrectNamespaceAndKey() {
            NamespacedKey key = LocalePlayerSetting.SETTING_KEY;
            assertEquals(McRPGMethods.getMcRPGNamespace(), key.getNamespace());
            assertEquals("locale-setting", key.getKey());
        }

        @DisplayName("getSettingKey returns SETTING_KEY for CLIENT_LOCALE")
        @Test
        void getSettingKey_clientLocale_returnsSameKey() {
            assertEquals(LocalePlayerSetting.SETTING_KEY, LocaleSetting.CLIENT_LOCALE.getSettingKey());
        }

        @DisplayName("getSettingKey returns SETTING_KEY for SERVER_LOCALE")
        @Test
        void getSettingKey_serverLocale_returnsSameKey() {
            assertEquals(LocalePlayerSetting.SETTING_KEY, LocaleSetting.SERVER_LOCALE.getSettingKey());
        }

        @DisplayName("getSettingKey returns SETTING_KEY for SpecificLocaleSetting")
        @Test
        void getSettingKey_specificLocale_returnsSameKey() {
            SpecificLocaleSetting setting = new SpecificLocaleSetting("en");
            assertEquals(LocalePlayerSetting.SETTING_KEY, setting.getSettingKey());
        }
    }

    @Nested
    @DisplayName("onSettingChange")
    class OnSettingChange {

        @DisplayName("onSettingChange does not throw when player is not McRPGPlayer")
        @Test
        void onSettingChange_nonMcRPGPlayer_doesNotThrow() {
            CorePlayer nonMcRPGPlayer = mock(CorePlayer.class);
            LocaleSetting.CLIENT_LOCALE.onSettingChange(nonMcRPGPlayer, Optional.empty());
        }

        @DisplayName("onSettingChange does not throw when player has no Bukkit player")
        @Test
        void onSettingChange_noBukkitPlayer_doesNotThrow(McRPGPlayer mcRPGPlayer) {
            LocaleSetting.CLIENT_LOCALE.onSettingChange(mcRPGPlayer, Optional.empty());
        }

        @DisplayName("onSettingChange does not throw when player has no GUI open")
        @Test
        void onSettingChange_noGuiOpen_doesNotThrow(McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);
            LocaleSetting.CLIENT_LOCALE.onSettingChange(mcRPGPlayer, Optional.empty());
        }

        @DisplayName("onSettingChange does not throw when player has non-settings GUI open")
        @Test
        @SuppressWarnings("unchecked")
        void onSettingChange_nonSettingsGui_doesNotThrow(McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);
            Gui<McRPGPlayer> mockGui = mock(Gui.class);
            mcRPG.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI)
                    .trackPlayerGui(mcRPGPlayer, mockGui);
            LocaleSetting.CLIENT_LOCALE.onSettingChange(mcRPGPlayer, Optional.empty());
        }

        @DisplayName("onSettingChange does not throw when PlayerSettingGui is open")
        @Test
        void onSettingChange_settingsGuiOpen_doesNotThrow(McRPGPlayer mcRPGPlayer) {
            PlayerMock playerMock = addPlayerToServer(mcRPGPlayer);
            PlayerSettingGui settingGui = new PlayerSettingGui(mcRPGPlayer);
            playerMock.openInventory(settingGui.getInventory());
            mcRPG.registryAccess()
                    .registry(RegistryKey.MANAGER)
                    .manager(McRPGManagerKey.GUI)
                    .trackPlayerGui(mcRPGPlayer, settingGui);
            LocaleSetting.CLIENT_LOCALE.onSettingChange(mcRPGPlayer, Optional.empty());
        }

        @DisplayName("onSettingChange with old setting present does not throw")
        @Test
        void onSettingChange_withOldSetting_doesNotThrow(McRPGPlayer mcRPGPlayer) {
            addPlayerToServer(mcRPGPlayer);
            LocaleSetting.CLIENT_LOCALE.onSettingChange(mcRPGPlayer,
                    Optional.of(LocaleSetting.SERVER_LOCALE));
        }
    }
}
