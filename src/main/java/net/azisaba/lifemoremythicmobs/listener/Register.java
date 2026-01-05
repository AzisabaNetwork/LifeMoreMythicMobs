package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicConditionLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicReloadedEvent;
import io.lumine.xikage.mythicmobs.skills.placeholders.PlaceholderManager;
import net.azisaba.lifemoremythicmobs.condition.*;
import net.azisaba.lifemoremythicmobs.mechanic.*;
import net.azisaba.lifemoremythicmobs.placeholder.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class Register implements Listener {

    @EventHandler
    public void onMythicMechanicLoad(@NotNull MythicMechanicLoadEvent e)	{

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
        if ( mechanic.equalsIgnoreCase("SetVarDisplayName") || mechanic.equalsIgnoreCase("SetDisplayNameVar") ) {
            e.register(new SetDisplayNameVarMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("SetVarLoreLine") || mechanic.equalsIgnoreCase("SetLoreLineVar") ) {
            e.register(new SetLoreLineVarMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("VarSubstring") ) {
            e.register(new VarSubstringMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("VarExtractNumber") ) {
            e.register(new VarExtractNumberMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("VarReplaceRegex") ) {
            e.register(new VarReplaceRegexMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("MMLuckEval") ) {
            e.register(new MMLuckEvalMechanic(e.getConfig()));
        }

    }

    @EventHandler
    public void onMythicConditionLoad(@NotNull MythicConditionLoadEvent e) {

        String condition = e.getConditionName();

        if ( condition.equalsIgnoreCase("realtime") ) {
            e.register(new RealTimeConditions(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("varNotEquals") ) {
            e.register(new VarNotEqualsCondition(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("serverEquals") ) {
            e.register(new ServerEqualsCondition(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("HasEmptyInventorySlot") ) {
            e.register(new HasEmptyInventorySlotCondition(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("HasItem") ) {
            e.register(new HasItemCondition(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("BowTension") ) {
            e.register(new BowTensionCondition(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("PlayersInRadius") ) {
            e.register(new PlayersInRadiusCondition(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("DayOfWeek") ) {
            e.register(new DayOfWeekCondition(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("isPet") ) {
            e.register(new IsPetCondition(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("mmidStartsWith") ) {
            e.register(new ItemMMIDStartsWithCondition(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("mmidContains") ) {
            e.register(new ItemMMIDContainsCondition(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("valCompare") ||
                condition.equalsIgnoreCase("valCompares") ||
                condition.equalsIgnoreCase("compareValues") ||
                condition.equalsIgnoreCase("compareValue")
        ) {
            e.register(new ValCompareCondition(e.getConfig()));
        }
        if ( condition.equalsIgnoreCase("itemInSlot") ) {
            e.register(new ItemInSlotCondition(e.getConfig()));
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
        ServerNamePlaceholder.register(manager);
        CasterArmorPlaceholder.register(manager);
        CasterAttackPlaceholder.register(manager);
    }

}
