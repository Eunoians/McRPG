package us.eunoians.mcrpg.exception;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import us.eunoians.mcrpg.ability.Ability;

public class EventNotRegisteredForActivation extends RuntimeException {

    private final Event event;
    private final Ability ability;

    public EventNotRegisteredForActivation(@NotNull Event event, @NotNull Ability ability) {
        this.event = event;
        this.ability = ability;
    }
}
