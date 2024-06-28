package us.eunoians.mcrpg.api.event.ability;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.Ability;
import us.eunoians.mcrpg.entity.holder.AbilityHolder;

/**
 * This event is called whenever an {@link Ability} activates
 */
public class AbilityActivateEvent extends AbilityEvent {

    private static final HandlerList handlers = new HandlerList();

    private final AbilityHolder abilityHolder;

    public AbilityActivateEvent(@NotNull AbilityHolder abilityHolder, @NotNull Ability ability) {
        super(ability);
        this.abilityHolder = abilityHolder;
    }

    /**
     * Gets the {@link AbilityHolder} that activated the {@link #getAbility() Ability}
     *
     * @return The {@link AbilityHolder} that activated the {@link #getAbility() Ability}
     */
    @NotNull
    public AbilityHolder getAbilityHolder() {
        return abilityHolder;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
