package douplo.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;


public interface PlayerRespawnCallback {

    Event<PlayerRespawnCallback> EVENT = EventFactory.createArrayBacked(PlayerRespawnCallback.class, (listeners) -> (player) -> {
       for (PlayerRespawnCallback c : listeners) {
           ActionResult ar = c.onRespawn(player);
           if (ar != ActionResult.PASS)
               return ar;
       }
       return ActionResult.PASS;
    });

    ActionResult onRespawn(ServerPlayerEntity player);

}
