package lifemoremythicmobs.org.example.lifemoremythicmobs.register;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicConditionLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicReloadedEvent;
import lifemoremythicmobs.org.example.lifemoremythicmobs.condition.RealTimeConditions;
import lifemoremythicmobs.org.example.lifemoremythicmobs.mechanic.ParticleVerticalRingMechanic;
import lifemoremythicmobs.org.example.lifemoremythicmobs.mechanic.TakeItemMechanic;
import lifemoremythicmobs.org.example.lifemoremythicmobs.placeholder.MMIDPlaceholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Register implements Listener {

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent e)	{

        String mechanic = e.getMechanicName();

        if( mechanic.equalsIgnoreCase("takeinv") ) {
            e.register(new TakeItemMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("particleverticalring") || mechanic.equalsIgnoreCase("pvr") || mechanic.equalsIgnoreCase("pvring") ) {
            e.register(new ParticleVerticalRingMechanic(e.getConfig()));
        }

    }

    @EventHandler
    public void onMythicConditionLoad(MythicConditionLoadEvent e) {

        String condition = e.getConditionName();

        if ( condition.equalsIgnoreCase("realtime") ) {
            e.register(new RealTimeConditions(e.getConfig()));
        }

    }

    @EventHandler
    public void onMythicPlaceholderLoad(MythicReloadedEvent e) {

        MMIDPlaceholder.register();

    }

}
