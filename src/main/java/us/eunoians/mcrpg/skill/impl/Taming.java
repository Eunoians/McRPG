package us.eunoians.mcrpg.skill.impl;

import us.eunoians.mcrpg.skill.AbstractSkill;
import us.eunoians.mcrpg.skill.OldSkill;

/**
 * This skill gains experience through {@link org.bukkit.entity.Wolf}s attacking various
 * {@link org.bukkit.entity.LivingEntity}s or a {@link org.bukkit.entity.Player} taming an
 * {@link org.bukkit.entity.Tameable} entity.
 * <p>
 * This {@link OldSkill} focuses on improving the abilities of {@link org.bukkit.entity.Wolf}s and is
 * primarily combat oriented.
 *
 * @author DiamondDagger590
 */
public class Taming extends AbstractSkill {

    /**
     * Construct a new {@link AbstractSkill}.
     *
     * @param id the id of the skill
     */
    public Taming(String id) {
        super(id);
    }

    @Override
    public void load() {
        // DO CONFIG STUFF HERE
    }
}
