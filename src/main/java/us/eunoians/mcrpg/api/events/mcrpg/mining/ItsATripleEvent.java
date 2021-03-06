package us.eunoians.mcrpg.api.events.mcrpg.mining;

import us.eunoians.mcrpg.abilities.mining.ItsATriple;
import us.eunoians.mcrpg.api.events.mcrpg.AbilityActivateEvent;
import us.eunoians.mcrpg.players.McRPGPlayer;
import us.eunoians.mcrpg.types.AbilityEventType;

public class ItsATripleEvent extends AbilityActivateEvent {

  public ItsATripleEvent(McRPGPlayer player, ItsATriple itsATriple){
    super(itsATriple, player, AbilityEventType.RECREATIONAL);
  }
}
