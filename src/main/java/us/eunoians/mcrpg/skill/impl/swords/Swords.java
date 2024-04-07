package us.eunoians.mcrpg.skill.impl.swords;

import org.bukkit.NamespacedKey;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.McRPG;
import us.eunoians.mcrpg.skill.Skill;

/**
 * A {@link Skill} that focuses on the usage of Minecraft Swords.
 *
 * Players will gain experience by attacking mobs with swords and unlock abilities focused
 * on the {@link us.eunoians.mcrpg.ability.impl.swords.Bleed} mechanic.
 */
public class Swords extends Skill {

    public static final NamespacedKey SWORDS_KEY = new NamespacedKey(McRPG.getInstance(), "swords");

    public Swords() {
        super(SWORDS_KEY);
        addLevelableComponent(SwordsComponents.SWORDS_LEVEL_ON_ATTACK_COMPONENT, EntityDamageByEntityEvent.class, 0);
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Swords";
    }

    @Override
    public int getMaxLevel() {
        return 1000;
    }
}
