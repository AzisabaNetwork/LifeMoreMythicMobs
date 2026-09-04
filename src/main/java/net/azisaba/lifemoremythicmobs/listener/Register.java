package net.azisaba.lifemoremythicmobs.listener;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.placeholders.PlaceholderManager;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import io.lumine.mythic.bukkit.events.MythicMobDespawnEvent;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import io.lumine.mythic.bukkit.events.MythicTargeterLoadEvent;
import io.lumine.mythic.core.skills.SkillExecutor;
import net.azisaba.lifemoremythicmobs.util.PlaceholderUtil;
import net.azisaba.lifemoremythicmobs.util.TimerRepository;
import net.azisaba.lifemoremythicmobs.util.TimerService;
import net.azisaba.lifemoremythicmobs.condition.*;
import net.azisaba.lifemoremythicmobs.conditions.*;
import net.azisaba.lifemoremythicmobs.mechanic.*;
import net.azisaba.lifemoremythicmobs.mechanic.mahjong.*;
import net.azisaba.lifemoremythicmobs.placeholder.*;
import net.azisaba.lifemoremythicmobs.placeholders.*;
import net.azisaba.lifemoremythicmobs.targeter.SphereTargeter;
import net.azisaba.lifemoremythicmobs.targeters.*;
import net.azisaba.lifemoremythicmobs.util.CustomAura;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Register implements Listener {
    private static TimerService timerService;
    private static TimerRepository timerRepository;

    public Register() {
    }

    public Register(TimerService timerService, TimerRepository timerRepository) {
        Register.timerService = timerService;
        Register.timerRepository = timerRepository;
    }

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
        if ( mechanic.equalsIgnoreCase("lmonDeath") || mechanic.equalsIgnoreCase("onDeathAura") ) {
            e.register(new OnDeathAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("onKill") || mechanic.equalsIgnoreCase("onKillAura") ) {
            e.register(new OnKillAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("onConsume") || mechanic.equalsIgnoreCase("onConsumeAura") ) {
            e.register(new OnConsumeAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("removeCustomAura") || mechanic.equalsIgnoreCase("removeCAura") ) {
            e.register(new RemoveCustomAuraMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("modifyPlayerAttribute") || mechanic.equalsIgnoreCase("modPAttribute") || mechanic.equalsIgnoreCase("modPlayerAttribute") ) {
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
        if (mechanic.equalsIgnoreCase("changeMythicItem") || mechanic.equalsIgnoreCase("mmchange")) {
            e.register(new ChangeMythicItem(executor, config));
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
        if ( mechanic.equalsIgnoreCase("typeOffensiveBuff") || mechanic.equalsIgnoreCase("tOffensiveBuff") ) {
            e.register(new TypeOffensiveBuffMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("typeDamage") || mechanic.equalsIgnoreCase("tDamage") ) {
            e.register(new TypedDamageMechanic(executor, config));
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
            e.register(new ExtendReachBuffMechanic(executor, config));
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
        if ( mechanic.equalsIgnoreCase("ModifyAttribute") || mechanic.equalsIgnoreCase("modAttribute") ) {
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
        if ( mechanic.equalsIgnoreCase("ParticleOrbitalCustom") ) {
            e.register(new ParticleOrbitalCustomEffect(executor, config));
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
        if ( mechanic.equalsIgnoreCase("lmVSkill") ) {
            e.register(new VSkillMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("HudText") ) {
            e.register(new HudTextMechanic(executor, config));
        }
        if ( mechanic.equalsIgnoreCase("lmProjectileVelocity") ) {
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
        if ( mechanic.equalsIgnoreCase("lmOnSwing") ) {
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
        if (condition.startsWith("?")) {
            condition = condition.substring(1).trim();
        }
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
        if ( condition.equalsIgnoreCase("lmHasItem") ) {
            e.register(new HasItemCondition(config));
        }
        if ( condition.equalsIgnoreCase("lmBowTension") || condition.equalsIgnoreCase("bowTension") || condition.equalsIgnoreCase("bowshoottension") ) {
            e.register(new BowTensionCondition(config));
        }
        if ( condition.equalsIgnoreCase("lmPlayersInRadius") ) {
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
                condition.equalsIgnoreCase("lmcompareValues") ||
                condition.equalsIgnoreCase("lmcompareValue") ||
                condition.equalsIgnoreCase("compareValues") ||
                condition.equalsIgnoreCase("compareValue")
        ) {
            e.register(new ValCompareCondition(config));
        }
        if ( condition.equalsIgnoreCase("itemInSlot") ) {
            e.register(new ItemInSlotCondition(config));
        }
        if ( condition.equalsIgnoreCase("ItemLore") ) {
            e.register(new ItemLoreCondition(config));
        }
        if ( condition.equalsIgnoreCase("typeBuffStacks") || condition.equalsIgnoreCase("tBuffStacks") ) {
            e.register(new TypeBuffStacksCondition(config));
        }
        if ( condition.equalsIgnoreCase("hasTypeBuff") || condition.equalsIgnoreCase("hastBuff") ) {
            e.register(new HasTypeBuffCondition(config));
        }
        if ( condition.equalsIgnoreCase("hasTypeOffensiveBuff") || condition.equalsIgnoreCase("hastOffensiveBuff") ) {
            e.register(new HasTypeOffensiveBuffCondition(config));
        }
        if ( condition.equalsIgnoreCase("typeOffensiveBuffStacks") || condition.equalsIgnoreCase("tOffensiveBuffStacks") ) {
            e.register(new TypeOffensiveBuffStacksCondition(config));
        }
        if ( condition.equalsIgnoreCase("isOnGlobalCooldown") || condition.equalsIgnoreCase("isOnGCD") || condition.equalsIgnoreCase("onGCD") ) {
            e.register(new IsOnGlobalCooldownCondition(e.getConfig()));
        }

        // === IgaCustom conditions ===
        if (condition.equalsIgnoreCase("cuboidCustom")) e.register(new CuboidCustomCondition(config.getLine(), config));
        if (condition.equalsIgnoreCase("notHasAura") || condition.equalsIgnoreCase("nothasaura")) e.register(new NotHasAuraCondition(config));
        if (condition.equalsIgnoreCase("HasAttribute")) e.register(new HasAttributeCondition(config));
        if (condition.equalsIgnoreCase("NearbyEntity")) e.register(new NearbyEntityCondition(config));
        if (condition.equalsIgnoreCase("ChinChiroMenashi")) e.register(new ChinChiroMenashiCondition(config.getLine(), config));
        if (condition.equalsIgnoreCase("HutagoAngelNearby")) e.register(new HutagoAngelNearbyCondition(config));
        if (condition.equalsIgnoreCase("lmGamemode")) e.register(new GamemodeCondition(config));
        if (condition.equalsIgnoreCase("Not")) e.register(new NotCondition(config));
        if (condition.equalsIgnoreCase("And")) e.register(new AndCondition(config));
        if (condition.equalsIgnoreCase("Or")) e.register(new OrCondition(config));
        if (condition.equalsIgnoreCase("WorldNotInConfig")) e.register(new WorldNotInConfigCondition(config.getLine(), config));
        if (condition.equalsIgnoreCase("ChinChiro456")) e.register(new ChinChiro456Condition(config.getLine(), config));
        if (condition.equalsIgnoreCase("HealthCompare")) e.register(new HealthCompareCondition(config.getLine(), config));
        if (condition.equalsIgnoreCase("WearingSlot") || condition.equalsIgnoreCase("wearing") || condition.equalsIgnoreCase("wearingslot")) e.register(new WearingSlotCondition(config.getLine(), config));
        if (condition.equalsIgnoreCase("HasMythicItem") || condition.equalsIgnoreCase("hasmmitem") || condition.equalsIgnoreCase("hasitemcustom")) e.register(new HasMythicItemCondition(config));
        if (condition.equalsIgnoreCase("warmup")) e.register(new WarmupCondition(config));
        if (condition.equalsIgnoreCase("triggerwithin")) e.register(new TriggerWithinCondition(config, false));
        if (condition.equalsIgnoreCase("triggernotwithin")) e.register(new TriggerWithinCondition(config, true));
    }

    @EventHandler
    public void onMythicPlaceholderLoad(MythicReloadedEvent e) {
        reloadPlaceholders();
    }

    public static void reloadPlaceholders() {
        PlaceholderManager manager = MythicBukkit.inst().getPlaceholderManager();
        PlaceholderUtil.withInitializedSuppressed(manager, () -> {
            MMIDPlaceholder.register(manager);
            ItemTagPlaceholder.register(manager);
            ServerNamePlaceholder.register(manager);
            CasterArmorPlaceholder.register(manager);
            CasterAttackPlaceholder.register(manager);
            CasterLuckPlaceholder.register(manager);
            PvELevelPlaceholder.register(manager);
            PotionLevelPlaceholder.register(manager);
            ConfigStringPlaceholder.register(manager);
            OriginLocationXPlaceholder.register(manager);
            OriginLocationYPlaceholder.register(manager);
            OriginLocationZPlaceholder.register(manager);
            if (timerService != null && timerRepository != null) {
                TimerElapsedPlaceholder.register(manager, timerService, timerRepository);
            }
        });
    }

    @EventHandler
    public void onMythicTargeterLoad(@NotNull MythicTargeterLoadEvent e) {
        String targeter = e.getTargeterName();
        if (targeter.startsWith("@")) {
            targeter = targeter.substring(1).trim();
        }
        // Existing targeters
        if ( targeter.equalsIgnoreCase("lmSphere") ) {
            e.register(new SphereTargeter(e.getContainer().getManager(), e.getConfig()));
        }
        // IgaCustom targeters
        SkillExecutor executor = e.getContainer().getManager();
        MythicLineConfig config = e.getConfig();
        if (targeter.equalsIgnoreCase("lmringAroundOrigin")) e.register(new RingAroundOriginTargeter(executor, config));
        if (targeter.equalsIgnoreCase("SummonsOfCaster")) e.register(new SummonsOfCasterTargeter(executor, config));
        if (targeter.equalsIgnoreCase("DirectionalOffset")) e.register(new DirectionalOffsetTargeter(executor, config));
        if (targeter.equalsIgnoreCase("PlayersInRadiusLimitVariable")) e.register(new PlayersInRadiusLimitVariableTargeter(executor, config));
        if (targeter.equalsIgnoreCase("PlayersFacingCaster")) e.register(new PlayersFacingCasterTargeter(executor, config));
        if (targeter.equalsIgnoreCase("AngleOffsetLocation")) e.register(new AngleOffsetLocationTargeter(executor, config));
        if (targeter.equalsIgnoreCase("EntitiesNearOriginCustom") || targeter.equalsIgnoreCase("ENOC")) e.register(new EntitiesNearOriginCustomTargeter(executor, config));
        if (targeter.equalsIgnoreCase("RandomAroundCasterLocation")) e.register(new RandomAroundCasterLocationTargeter(executor, config));
        if (targeter.equalsIgnoreCase("RandomOriginPoints") || targeter.equalsIgnoreCase("rop") || targeter.equalsIgnoreCase("randomorigin")) e.register(new RandomOriginPointsTargeter(executor, config));
        if (targeter.equalsIgnoreCase("LivingInRadiusCustom") || targeter.equalsIgnoreCase("livingEntitiesInRadiusCustom") || targeter.equalsIgnoreCase("entitiesInRadiusCustom") || targeter.equalsIgnoreCase("EIRC")) e.register(new LivingInRadiusCustomTargeter(executor, config));
        if (targeter.equalsIgnoreCase("~onAttack") || targeter.equalsIgnoreCase("onAttack")) {
            e.register(new io.lumine.mythic.core.skills.targeters.IEntitySelector(executor, config) {
                @Override
                public java.util.HashSet<io.lumine.mythic.api.adapters.AbstractEntity> getEntities(io.lumine.mythic.api.skills.SkillMetadata data) {
                    java.util.HashSet<io.lumine.mythic.api.adapters.AbstractEntity> targets = new java.util.HashSet<>();
                    if (data != null) {
                        if (data.getTrigger() != null) {
                            targets.add(data.getTrigger());
                        } else if (data.getCaster() != null && data.getCaster().getEntity() != null) {
                            targets.add(data.getCaster().getEntity());
                        }
                    }
                    return targets;
                }
            });
        }
    }

    @EventHandler
    public void onMythicMobDeath(MythicMobDeathEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        TypeBuffMechanic.removeAll(uuid);
        TypeOffensiveBuffMechanic.removeAll(uuid);
        CustomAura.removeAll(uuid);
    }

    @EventHandler
    public void onMythicMobDespawn(MythicMobDespawnEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        TypeBuffMechanic.removeAll(uuid);
        TypeOffensiveBuffMechanic.removeAll(uuid);
        CustomAura.removeAll(uuid);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        TypeBuffMechanic.removeAll(uuid);
        TypeOffensiveBuffMechanic.removeAll(uuid);
        CustomAura.removeAll(uuid);
    }
}
