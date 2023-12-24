package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicConditionLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicReloadedEvent;
import io.lumine.xikage.mythicmobs.skills.placeholders.PlaceholderManager;
import net.azisaba.lifemoremythicmobs.condition.RealTimeConditions;
import net.azisaba.lifemoremythicmobs.condition.VarNotEqualsCondition;
import net.azisaba.lifemoremythicmobs.mechanic.*;
import net.azisaba.lifemoremythicmobs.placeholder.ItemTagPlaceholder;
import net.azisaba.lifemoremythicmobs.placeholder.MMIDPlaceholder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class Register implements Listener {

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent e)	{

        String mechanic = e.getMechanicName();

        if ( mechanic.equalsIgnoreCase("takeinv") ) {
            e.register(new TakeItemMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("particleverticalring") || mechanic.equalsIgnoreCase("pvr") || mechanic.equalsIgnoreCase("pvring") ) {
            e.register(new ParticleVerticalRingMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("bossbar") ) {
            e.register(new BossBarMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("removebossbar") || mechanic.equalsIgnoreCase("bossbarremove") ) {
            e.register(new RemoveBossBarMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("modifybossbar") || mechanic.equalsIgnoreCase("bossbarmodify")) {
            e.register(new ModifyBossBarMechanic(e.getConfig()));
        }

    }

    @EventHandler
    public void onMythicConditionLoad(MythicConditionLoadEvent e) {

        String condition = e.getConditionName();

        if ( condition.equalsIgnoreCase("realtime") ) {
            e.register(new RealTimeConditions(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("varNotEquals") ) {
            e.register(new VarNotEqualsCondition(e.getConfig()));
        }

    }

    @EventHandler
    public void onMythicPlaceholderLoad(MythicReloadedEvent e) {
        reloadPlaceholders();
    }

    public static void reloadPlaceholders() {
        PlaceholderManager manager = MythicMobs.inst().getPlaceholderManager();
        MMIDPlaceholder.register(manager);
        ItemTagPlaceholder.register(manager);
    }

}
