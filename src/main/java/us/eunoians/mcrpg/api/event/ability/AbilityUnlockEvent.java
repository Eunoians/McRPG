package us.eunoians.mcrpg.api.event.ability;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.impl.UnlockableAbility;

/**
 * This event is called whenever an ability holder unlocks a new ability
 */
public class AbilityUnlockEvent extends AbilityEvent {

    private static final HandlerList handlers = new HandlerList();

    public AbilityUnlockEvent(@NotNull UnlockableAbility ability) {
        super(ability);
    }

    @NotNull
    @Override
    public UnlockableAbility getAbility() {
        return (UnlockableAbility) super.getAbility();
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