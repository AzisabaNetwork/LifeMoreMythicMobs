package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillManager;
import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import io.lumine.mythic.bukkit.events.MythicTargeterLoadEvent;
import io.lumine.mythic.core.skills.SkillExecutor;
import org.bukkit.event.entity.EntityDeathEvent;
import net.azisaba.lifemoremythicmobs.condition.*;
import net.azisaba.lifemoremythicmobs.mechanic.*;
import net.azisaba.lifemoremythicmobs.placeholder.*;
import net.azisaba.lifemoremythicmobs.targeter.SphereTargeter;
import net.azisaba.lifemoremythicmobs.util.CustomAura;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Register implements Listener {

    @EventHandler
    public void onMythicMechanicLoad(@NotNull MythicMechanicLoadEvent e)	{

        String mechanic = e.getMechanicName();
        MythicLineConfig config = e.getConfig();
        SkillExecutor executor = e.getContainer().getManager();

        if ( mechanic.equalsIgnoreCase("takeinv") ) {
            e.register(new TakeItemMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("particleverticalring") || mechanic.equalsIgnoreCase("pvr") || mechanic.equalsIgnoreCase("pvring") ) {
            e.register(new ParticleVerticalRingMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("bossbar") ) {
            e.register(new BossBarMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("removebossbar") || mechanic.equalsIgnoreCase("bossbarremove") ) {
            e.register(new RemoveBossBarMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("modifybossbar") || mechanic.equalsIgnoreCase("bossbarmodify")) {
            e.register(new ModifyBossBarMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("SetVarDisplayName") || mechanic.equalsIgnoreCase("SetDisplayNameVar") ) {
            e.register(new SetDisplayNameVarMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("SetVarLoreLine") || mechanic.equalsIgnoreCase("SetLoreLineVar") ) {
            e.register(new SetLoreLineVarMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("VarSubstring") ) {
            e.register(new VarSubstringMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("VarExtractNumber") ) {
            e.register(new VarExtractNumberMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("VarReplaceRegex") ) {
            e.register(new VarReplaceRegexMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("nullrecovery") ) {
            e.register(new NullRecoveryMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("slotjam") ) {
            e.register(new SlotJamMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("fakesounddistortion") || mechanic.equalsIgnoreCase("fakesound") ) {
            e.register(new FakeSoundDistortionMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("setFirstPersonView") || mechanic.equalsIgnoreCase("sfpv") ) {
            e.register(new SetFirstPersonViewMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("fakeWorldBorder") || mechanic.equalsIgnoreCase("fakeborder")  ) {
            e.register(new FakeWorldBorderMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("skyRotation") || mechanic.equalsIgnoreCase("skyrotate") ) {
            e.register(new SkyRotationMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("fakeblock") ) {
            e.register(new FakeBlockMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("onDeath") || mechanic.equalsIgnoreCase("onDeathAura") ) {
            e.register(new OnDeathAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("onKill") || mechanic.equalsIgnoreCase("onKillAura") ) {
            e.register(new OnKillAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("onConsume") || mechanic.equalsIgnoreCase("onConsumeAura") ) {
            e.register(new OnConsumeAuraMechanic(e.getConfig()));
        }
        if ( mechanic.equalsIgnoreCase("removeCustomAura") || mechanic.equalsIgnoreCase("removeCAura") ) {
            e.register(new RemoveCustomAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("modifyPlayerAttribute") || mechanic.equalsIgnoreCase("modPAttribute") ) {
            e.register(new ModifyAttributeMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("MMLuckEval") ) {
            e.register(new MMLuckEvalMechanic(executor, config));
        }
        if (mechanic.equalsIgnoreCase("varOnSwing") || mechanic.equalsIgnoreCase("vOnSwing") ) {
            e.register(new VarOnInteractAuraMechanic(executor, config, true));
        }
        if (mechanic.equalsIgnoreCase("varOnUse") || mechanic.equalsIgnoreCase("vOnUse") ) {
            e.register(new VarOnInteractAuraMechanic(executor, config, false));
        }
        if (mechanic.equalsIgnoreCase("LockInventory") || mechanic.equalsIgnoreCase("LockInv") ) {
            e.register(new LockInventoryMechanic(executor, config));
        }
        if (mechanic.equalsIgnoreCase("newRandomSkill") || mechanic.equalsIgnoreCase("nRandomSkill") ) {
            e.register(new NewRandomSkillMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("changeItemNBT") ) {
            e.register(new ChangeItemNBTMechanic(executor, config));
        }
        if (mechanic.equalsIgnoreCase("getItemNBT")) {
            e.register(new GetItemNBTMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("CastVariable") || mechanic.equalsIgnoreCase("castVar") ) {
            e.register(new VariableCastMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("DispatchVariableSkill") || mechanic.equalsIgnoreCase("disVarSkill") ) {
            e.register(new DispatchVariableSkillMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("CallWithArgs") || mechanic.equalsIgnoreCase("CallArgs") ) {
            e.register(new CallWithArgsMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("PersistentZone") || mechanic.equalsIgnoreCase("PerZone") ) {
            e.register(new PersistentZoneMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("BouncingRaytrace") || mechanic.equalsIgnoreCase("bRaytrace") ) {
            e.register(new BouncingRaytraceMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ShapeRenderer") || mechanic.equalsIgnoreCase("lShape") ) {
            e.register(new ShapeRendererMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("typeBuff") || mechanic.equalsIgnoreCase("tBuff") ) {
            e.register(new TypeBuffMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("typeDamage") || mechanic.equalsIgnoreCase("tDamage") ) {
            e.register(new TypedDamageMechanic(executor, config));
        }
    }

    @EventHandler
    public void onMythicConditionLoad(@NotNull MythicConditionLoadEvent e) {

        String condition = e.getConditionName();
        MythicLineConfig config = e.getConfig();

        if ( condition.equalsIgnoreCase("realtime") ) {
            e.register(new RealTimeConditions(config));
        }
        if ( condition.equalsIgnoreCase("varNotEquals") ) {
            e.register(new VarNotEqualsCondition(config));
        }
        if ( condition.equalsIgnoreCase("serverEquals") ) {
            e.register(new ServerEqualsCondition(config));
        }
        if ( condition.equalsIgnoreCase("HasEmptyInventorySlot") ) {
            e.register(new HasEmptyInventorySlotCondition(config));
        }
        if ( condition.equalsIgnoreCase("HasItem") ) {
            e.register(new HasItemCondition(config));
        }
        if ( condition.equalsIgnoreCase("BowTension") ) {
            e.register(new BowTensionCondition(config));
        }
        if ( condition.equalsIgnoreCase("PlayersInRadius") ) {
            e.register(new PlayersInRadiusCondition(config));
        }
        if ( condition.equalsIgnoreCase("DayOfWeek") ) {
            e.register(new DayOfWeekCondition(config));
        }
        if ( condition.equalsIgnoreCase("isPet") ) {
            e.register(new IsPetCondition(config));
        }
        if ( condition.equalsIgnoreCase("mmidStartsWith") ) {
            e.register(new ItemMMIDStartsWithCondition(config));
        }
        if ( condition.equalsIgnoreCase("mmidContains") ) {
            e.register(new ItemMMIDContainsCondition(config));
        }
        if ( condition.equalsIgnoreCase("valCompare") ||
                condition.equalsIgnoreCase("valCompares") ||
                condition.equalsIgnoreCase("compareValues") ||
                condition.equalsIgnoreCase("compareValue")
        ) {
            e.register(new ValCompareCondition(config));
        }
        if ( condition.equalsIgnoreCase("itemInSlot") ) {
            e.register(new ItemInSlotCondition(config));
        }
        if ( condition.equalsIgnoreCase("typeBuffStacks") || condition.equalsIgnoreCase("tBuffStacks") ) {
            e.register(new TypeBuffStacksCondition(config));
        }
        if ( condition.equalsIgnoreCase("hasTypeBuff") || condition.equalsIgnoreCase("hastBuff") ) {
            e.register(new HasTypeBuffCondition(config));
        }
    }

    @EventHandler
    public void onMythicPlaceholderLoad(MythicReloadedEvent e) {
        reloadPlaceholders();
    }

    public static void reloadPlaceholders() {
        PlaceholderManager manager = MythicBukkit.inst().getPlaceholderManager();
        MMIDPlaceholder.register(manager);
        ItemTagPlaceholder.register(manager);
        ServerNamePlaceholder.register(manager);
        CasterArmorPlaceholder.register(manager);
        CasterAttackPlaceholder.register(manager);
        CasterLuckPlaceholder.register(manager);
        PvELevelPlaceholder.register(manager);
    }

    @EventHandler
    public void onMythicTargeterLoad(@NotNull MythicTargeterLoadEvent e) {
        String targeter = e.getTargeterName();
        if ( targeter.equalsIgnoreCase("Sphere") ) {
            e.register(new SphereTargeter(e.getContainer().getManager(), e.getConfig()));
        }
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        TypeBuffMechanic.removeAll(uuid);
        CustomAura.removeAll(uuid);
    }

    @EventHandler
    public void onMythicMobDespawn(MythicMobDespawnEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        TypeBuffMechanic.removeAll(uuid);
        CustomAura.removeAll(uuid);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        TypeBuffMechanic.removeAll(uuid);
        CustomAura.removeAll(uuid);
    }
}
