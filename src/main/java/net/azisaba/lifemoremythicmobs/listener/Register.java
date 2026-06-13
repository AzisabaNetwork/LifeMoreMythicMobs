package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillManager;
import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import io.lumine.mythic.bukkit.events.MythicTargeterLoadEvent;
import io.lumine.mythic.core.skills.SkillExecutor;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import net.azisaba.lifemoremythicmobs.condition.*;
import net.azisaba.lifemoremythicmobs.mechanic.*;
import net.azisaba.lifemoremythicmobs.mechanic.mahjong.*;
import net.azisaba.lifemoremythicmobs.placeholder.*;
import net.azisaba.lifemoremythicmobs.targetter.SphereTargeter;
import net.azisaba.lifemoremythicmobs.targeters.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class Register implements Listener {

    @EventHandler
    public void onMythicMechanicLoad(@NotNull MythicMechanicLoadEvent e) {

        String mechanic = e.getMechanicName();
        MythicLineConfig config = e.getConfig();
        SkillExecutor executor = e.getContainer().getManager();

        // === Existing LifeMore mechanics ===
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
        if ( mechanic.equalsIgnoreCase("removeCustomAura") || mechanic.equalsIgnoreCase("removeCAura") ) {
            e.register(new RemoveCustomAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("modifyPlayerAttribute") || mechanic.equalsIgnoreCase("modPAttribute") ) {
            e.register(new ModifyPlayerAttributeMechanic(executor, config));
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
        if ( mechanic.equalsIgnoreCase("modifybossbar") || mechanic.equalsIgnoreCase("bossbarmodify") ) {
            e.register(new ModifyBossBarMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("OnKillAura") || mechanic.equalsIgnoreCase("onKillAura") ) {
            e.register(new OnKillAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("removeCustomAura") || mechanic.equalsIgnoreCase("removeCAura") ) {
            e.register(new RemoveCustomAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("modifyPlayerAttribute") || mechanic.equalsIgnoreCase("modPAttribute") ) {
            e.register(new ModifyPlayerAttributeMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("MMLuckEval") ) {
            e.register(new MMLuckEvalMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("varOnSwing") || mechanic.equalsIgnoreCase("vOnSwing") ) {
            e.register(new VarOnInteractAuraMechanic(executor, config, true));
        }
        if ( mechanic.equalsIgnoreCase("varOnUse") || mechanic.equalsIgnoreCase("vOnUse") ) {
            e.register(new VarOnInteractAuraMechanic(executor, config, false));
        }
        if ( mechanic.equalsIgnoreCase("LockInventory") || mechanic.equalsIgnoreCase("LockInv") ) {
            e.register(new LockInventoryMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("newRandomSkill") || mechanic.equalsIgnoreCase("nRandomSkill") ) {
            e.register(new NewRandomSkillMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("changeItemNBT") ) {
            e.register(new ChangeItemNBTMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("getItemNBT") ) {
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

        // === IgaCustom mechanics ===
        if ( mechanic.equalsIgnoreCase("VarReplaceRegexCustom") ) {
            e.register(new VarReplaceRegexCustomMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("PotionClearCustom") ) {
            e.register(new PotionClearCustomMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("JsonGetter") ) {
            e.register(new JsonGetterMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("JsonSetter") ) {
            e.register(new JsonSetterMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("WallPhase") ) {
            e.register(new WallPhaseMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("CubeTeleport") ) {
            e.register(new CubeTeleportMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ExtendAttackReach") ) {
            e.register(new ExtendAttackReachMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ExtendReachBuff") ) {
            ExtendReachBuffMechanic extInst = new ExtendReachBuffMechanic(executor, config);
            e.register(extInst);
            Bukkit.getPluginManager().registerEvents(extInst, JavaPlugin.getPlugin(LifeMoreMythicMobs.class));
        }
        if ( mechanic.equalsIgnoreCase("Slach") ) {
            e.register(new SlachMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ParticleFan") ) {
            e.register(new ParticleFanMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ChargeBossBar") ) {
            e.register(new ChargeBossBarMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("HeightAura") ) {
            e.register(new HeightAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ParticleRandomEffect") ) {
            e.register(new ParticleRandomEffectMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("MahjongHaiyamaInit") ) {
            e.register(new MahjongHaiyamaInitMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ParticleTentacle") ) {
            e.register(new ParticleTentacleMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("TargetYawFromOrigin") ) {
            e.register(new TargetYawFromOriginMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("SetCustomModelData") ) {
            e.register(new SetCustomModelDataMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("WallAwareTeleport") ) {
            e.register(new WallAwareTeleportMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("Rhombus") ) {
            e.register(new RhombusMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("SpeakCustom") ) {
            e.register(new SpeakCustomMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("Rrraytrace") ) {
            e.register(new RrraytraceMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ModifyAttribute") ) {
            e.register(new ModifyAttributeMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("SetItemLore") ) {
            e.register(new SetItemLoreMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("SetDamageModifierAura") ) {
            e.register(new SetDamageModifierAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("SelectionFidoruGUI") ) {
            e.register(new SelectionFidoruGUIMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("RecoilView") ) {
            e.register(new RecoilViewMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("DirectionalOffsetToVariable") ) {
            e.register(new DirectionalOffsetToVariableMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("CalculateYawFromPositions") ) {
            e.register(new CalculateYawFromPositionsMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("CallSkillFromGUI") ) {
            e.register(new CallSkillFromGUIMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("GiveOwnHead") ) {
            e.register(new GiveOwnHeadMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ChinChiroJudge") ) {
            e.register(new ChinChiroJudgeMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("PrisonCustom") ) {
            e.register(new PrisonCustomMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("LotteryReward") ) {
            e.register(new LotteryRewardMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("DigitSplitRandom") ) {
            e.register(new DigitSplitRandomMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("FidoruOffhandCombine") ) {
            e.register(new FidoruOffhandCombineMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("RotteryRewardRedeeGui") ) {
            e.register(new RotteryRewardRedeeGuiMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ParticleSphereCustom") ) {
            e.register(new ParticleSphereCustomEffect(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("NamedTotem") ) {
            e.register(new NamedTotemMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("RemoveNamedTotem") ) {
            e.register(new RemoveNamedTotemMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("AngleToTarget") ) {
            e.register(new AngleToTargetMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("CustomProjectile") ) {
            e.register(new CustomProjectileMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("SpherePlace") ) {
            e.register(new SpherePlaceMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("KillMessageDamage") ) {
            e.register(new KillMessageDamageMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("RectParticleWall") ) {
            e.register(new RectParticleWallMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("DamageAs") ) {
            e.register(new DamageAsMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("SetMetaSkillVariable") ) {
            e.register(new SetMetaSkillVariableMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("VSkill") ) {
            e.register(new VSkillMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("HudText") ) {
            e.register(new HudTextMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ProjectileVelocity") ) {
            e.register(new ProjectileVelocityMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("RandomOrbitPoint") ) {
            e.register(new RandomOrbitPointMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("EllipseFollow") ) {
            e.register(new EllipseFollowMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ProjectileOrientationStore") ) {
            e.register(new ProjectileOrientationStoreMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("CharReorderGui") ) {
            e.register(new CharReorderGuiMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("DelayCall") ) {
            e.register(new DelayCallMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("AttributeBuff") ) {
            e.register(new AttributeBuffMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("HealBlock") ) {
            e.register(new HealBlockMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ReticleTittle") ) {
            e.register(new ReticleTittleMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("EquipLockAura") ) {
            e.register(new EquipLockAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("ParticleStar") ) {
            e.register(new ParticleStarEffect(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("GradientParticleEffect") ) {
            e.register(new GradientParticleEffectMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("OrbitalCustom") ) {
            e.register(new OrbitalCustomMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("DrawQuadParticle") ) {
            e.register(new DrawQuadParticleMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("JsonArrayPush") ) {
            e.register(new JsonArrayPushMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("OnAttackExtend") ) {
            e.register(new OnAttackExtendMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("OnSwing") ) {
            e.register(new OnSwingMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("SwitchCustom") ) {
            e.register(new SwitchCustomMechanic(executor, config));
        }
        // Add alias: customProjectile -> CustomProjectileMechanic
        if ( mechanic.equalsIgnoreCase("customProjectile") ) {
            e.register(new CustomProjectileMechanic(executor, config));
        }
    }

    @EventHandler
    public void onMythicConditionLoad(@NotNull MythicConditionLoadEvent e) {

        String condition = e.getConditionName();
        MythicLineConfig config = e.getConfig();

        // === Existing LifeMore conditions ===
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

        // === IgaCustom conditions ===
        if ( condition.equalsIgnoreCase("cuboidCustom") ) {
            e.register(new CuboidCustomCondition(config.getLine(), config));
        }
        if ( condition.equalsIgnoreCase("notHasAura") ) {
            e.register(new NotHasAuraCondition(config));
        }
        if ( condition.equalsIgnoreCase("HasAttribute") ) {
            e.register(new HasAttributeCondition(config));
        }
        if ( condition.equalsIgnoreCase("NearbyEntity") ) {
            e.register(new NearbyEntityCondition(config));
        }
        if ( condition.equalsIgnoreCase("ChinChiroMenashi") ) {
            e.register(new ChinChiroMenashiCondition(config.getLine(), config));
        }
        if ( condition.equalsIgnoreCase("HutagoAngelNearby") ) {
            e.register(new HutagoAngelNearbyCondition(config));
        }
        if ( condition.equalsIgnoreCase("Gamemode") ) {
            e.register(new GamemodeCondition(config));
        }
        if ( condition.equalsIgnoreCase("Not") ) {
            e.register(new NotCondition(config.getLine(), config));
        }
        if ( condition.equalsIgnoreCase("And") ) {
            e.register(new AndCondition(config.getLine(), config));
        }
        if ( condition.equalsIgnoreCase("Or") ) {
            e.register(new OrCondition(config.getLine(), config));
        }
        if ( condition.equalsIgnoreCase("WorldNotInConfig") ) {
            e.register(new WorldNotInConfigCondition(config.getLine(), config));
        }
        if ( condition.equalsIgnoreCase("ChinChiro456") ) {
            e.register(new ChinChiro456Condition(config.getLine(), config));
        }
        if ( condition.equalsIgnoreCase("HealthCompare") ) {
            e.register(new HealthCompareCondition(config.getLine(), config));
        }
        if ( condition.equalsIgnoreCase("WearingSlot") ) {
            e.register(new WearingSlotCondition(config.getLine(), config));
        }
    }

    @EventHandler
    public void onMythicPlaceholderLoad(MythicReloadedEvent e) {
        reloadPlaceholders();
    }

    public static void reloadPlaceholders() {
        PlaceholderManager manager = MythicBukkit.inst().getPlaceholderManager();
        // Existing placeholders
        MMIDPlaceholder.register(manager);
        ItemTagPlaceholder.register(manager);
        ServerNamePlaceholder.register(manager);
        CasterArmorPlaceholder.register(manager);
        CasterAttackPlaceholder.register(manager);
        CasterLuckPlaceholder.register(manager);
        PvELevelPlaceholder.register(manager);
        // IgaCustom placeholders
        OriginLocationXPlaceholder.register(manager);
        OriginLocationYPlaceholder.register(manager);
        OriginLocationZPlaceholder.register(manager);
    }

    @EventHandler
    public void onMythicTargeterLoad(@NotNull MythicTargeterLoadEvent e) {
        String targeter = e.getTargeterName();
        // Existing targeters
        if ( targeter.equalsIgnoreCase("Sphere") ) {
            e.register(new SphereTargeter(e.getContainer().getManager(), e.getConfig()));
        }
        // IgaCustom targeters
        if ( targeter.equalsIgnoreCase("ringAroundOrigin") ) {
            e.register(new RingAroundOriginTargeter(e.getConfig()));
        }
        if ( targeter.equalsIgnoreCase("SummonsOfCaster") ) {
            e.register(new SummonsOfCasterTargeter(e.getConfig()));
        }
        if ( targeter.equalsIgnoreCase("DirectionalOffset") ) {
            e.register(new DirectionalOffsetTargeter(e.getConfig()));
        }
        if ( targeter.equalsIgnoreCase("PlayersInRadiusLimitVariable") ) {
            e.register(new PlayersInRadiusLimitVariableTargeter(e.getConfig()));
        }
        if ( targeter.equalsIgnoreCase("PlayersFacingCaster") ) {
            e.register(new PlayersFacingCasterTargeter(e.getConfig()));
        }
        if ( targeter.equalsIgnoreCase("AngleOffsetLocation") ) {
            e.register(new AngleOffsetLocationTargeter(e.getConfig()));
        }
        if ( targeter.equalsIgnoreCase("EntitiesNearOriginCustom") ) {
            e.register(new EntitiesNearOriginCustomTargeter(e.getConfig()));
        }
        if ( targeter.equalsIgnoreCase("RandomAroundCasterLocation") ) {
            e.register(new RandomAroundCasterLocationTargeter(e.getConfig()));
        }
        if ( targeter.equalsIgnoreCase("RandomOriginPoints") ) {
            e.register(new RandomOriginPointsTargeter(e.getConfig()));
        }
        if ( targeter.equalsIgnoreCase("LivingInRadiusCustom") ) {
            e.register(new LivingInRadiusCustomTargeter(e.getConfig()));
        }
    }
}
