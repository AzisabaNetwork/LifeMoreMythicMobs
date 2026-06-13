package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.items.MythicItem;
import io.lumine.mythic.logging.MythicLogger;
import io.lumine.mythic.logging.MythicLogger.DebugLevel;
import io.lumine.mythic.mobs.ActiveMob;
import io.lumine.mythic.mobs.MythicMob;
import io.lumine.mythic.mobs.entities.SpawnReason;
import io.lumine.mythic.api.skills.AbstractSkill;
import io.lumine.mythic.api.skills.IParentSkill;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.auras.Aura;
import io.lumine.mythic.api.skills.auras.Aura.AuraTracker;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.api.skills.placeholders.PlaceholderFloat;
import io.lumine.mythic.api.skills.placeholders.PlaceholderInt;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.api.skills.projectiles.ProjectileHitBox;
import io.lumine.mythic.util.VectorUtils;
import io.lumine.mythic.utils.Schedulers;
import io.lumine.mythic.utils.items.ItemFactory;
import io.lumine.mythic.utils.version.MinecraftVersions;
import io.lumine.mythic.utils.version.ServerVersion;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

public class OrbitalCustomMechanic extends Aura implements ITargetedEntitySkill, ITargetedLocationSkill {
   public static final Set<AbstractEntity> BULLET_ENTITIES = ConcurrentHashMap.newKeySet();
   private final PlaceholderFloat radius;
   private final PlaceholderFloat hitRadius;
   private final PlaceholderFloat verticalHitRadius;
   private final PlaceholderInt points;
   private final PlaceholderInt startingStep;
   private final PlaceholderDouble xRotation;
   private final PlaceholderDouble yRotation;
   private final PlaceholderDouble zRotation;
   private final PlaceholderDouble xOffset;
   private final PlaceholderDouble yOffset;
   private final PlaceholderDouble zOffset;
   private final PlaceholderDouble angularVelocityX;
   private final PlaceholderDouble angularVelocityY;
   private final PlaceholderDouble angularVelocityZ;
   private final boolean rotate;
   private final boolean reversed;
   private final PlaceholderDouble rollDegrees;
   private final boolean hitSelf;
   private final boolean hitPlayers;
   private final boolean hitNonPlayers;
   private final String onHitSkillName;
   private final String bulletTypeName;
   private final String bulletMaterialName;
   private final int bulletModelId;
   private final String bulletMythicItemName;
   private final String bulletMobTypeName;
   private final Float bulletSpin;

   public OrbitalCustomMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.radius = phFloat(config, new String[]{"radius", "r"}, "4");
      this.hitRadius = phFloat(config, new String[]{"hitradius", "hr"}, "1");
      this.verticalHitRadius = phFloat(config, new String[]{"verticalhitradius", "vhr", "vr"}, null);
      this.points = phInt(config, new String[]{"points", "p"}, "32");
      this.startingStep = phInt(config, new String[]{"startingPoint", "sp"}, "0");
      this.xRotation = phDouble(config, new String[]{"rotationx", "rotx", "rx"}, "0");
      this.yRotation = phDouble(config, new String[]{"rotationy", "roty", "ry"}, "0");
      this.zRotation = phDouble(config, new String[]{"rotationz", "rotz", "rz"}, "0");
      this.xOffset = phDouble(config, new String[]{"offsetx", "offx", "ox"}, "0");
      this.yOffset = phDouble(config, new String[]{"offsety", "offy", "oy"}, "0");
      this.zOffset = phDouble(config, new String[]{"offsetz", "offz", "oz"}, "0");
      this.angularVelocityX = phDouble(config, new String[]{"angularvelocityx", "avx", "vx"}, "0");
      this.angularVelocityY = phDouble(config, new String[]{"angularvelocityy", "avy", "vy"}, "0");
      this.angularVelocityZ = phDouble(config, new String[]{"angularvelocityz", "avz", "vz"}, "0");
      this.rotate = config.getBoolean(new String[]{"rotate"}, false);
      this.reversed = config.getBoolean(new String[]{"reversed", "reverse", "backwards"}, false);
      this.rollDegrees = phDouble(config, new String[]{"roll", "rl"}, "0");
      this.hitSelf = config.getBoolean(new String[]{"hitself", "hs"}, false);
      this.hitPlayers = config.getBoolean(new String[]{"hitplayers", "hp"}, true);
      this.hitNonPlayers = config.getBoolean(new String[]{"hitnonplayers", "hnp"}, false);
      this.onHitSkillName = config.getString(new String[]{"onhitskill", "onhit", "oh"}, "", new String[0]);
      this.bulletTypeName = config.getString(new String[]{"bullettype", "bullet", "b"}, "NONE", new String[0]);
      this.bulletMaterialName = config.getString(new String[]{"bulletmaterial", "material", "mat"}, "STONE", new String[0]);
      this.bulletModelId = config.getInteger(new String[]{"bulletmodel", "model"}, 0);
      this.bulletMythicItemName = config.getString(new String[]{"bulletmythicitem", "mythicitem", "mi"}, "", new String[0]);
      this.bulletMobTypeName = config.getString(new String[]{"mob", "mobtype", "mm"}, "SleletonKing", new String[0]);
      this.bulletSpin = config.getFloat(new String[]{"bulletspin", "bspin"}, 0.0F);
   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      new OrbitalCustomMechanic.OrbitalCustomTracker(data, target);
      return SkillResult.SUCCESS;
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      new OrbitalCustomMechanic.OrbitalCustomTracker(data, target);
      return SkillResult.SUCCESS;
   }

   private static OrbitalCustomMechanic.BulletType parseBulletType(String s) {
      if (s == null) {
         return OrbitalCustomMechanic.BulletType.NONE;
      }

      try {
         return OrbitalCustomMechanic.BulletType.valueOf(s.trim().toUpperCase());
      } catch (Exception ignored) {
         return OrbitalCustomMechanic.BulletType.NONE;
      }
   }

   private static Material parseMaterial(String s) {
      if (s != null && !s.trim().isEmpty()) {
         try {
            return Material.valueOf(s.trim().toUpperCase());
         } catch (Exception ignored) {
            return Material.STONE;
         }
      } else {
         return Material.STONE;
      }
   }

   private static PlaceholderString phString(MythicLineConfig config, String[] keys, String def) {
      String raw = config.getString(keys, def, new String[0]);
      return PlaceholderString.of(raw);
   }

   private static PlaceholderInt phInt(MythicLineConfig config, String[] keys, String def) {
      String raw = config.getString(keys, def, new String[0]);
      return PlaceholderInt.of(raw);
   }

   private static PlaceholderFloat phFloat(MythicLineConfig config, String[] keys, String def) {
      String raw = def == null ? config.getString(keys, null, new String[0]) : config.getString(keys, def, new String[0]);
      return raw == null ? null : PlaceholderFloat.of(raw);
   }

   private static PlaceholderDouble phDouble(MythicLineConfig config, String[] keys, String def) {
      String raw = config.getString(keys, def, new String[0]);
      return PlaceholderDouble.of(raw);
   }

   private enum BulletType {
      NONE,
      ITEM,
      BLOCK,
      SMALLBLOCK,
      MYTHICITEM,
      ARMOR_STAND,
      ARROW,
      MOB;
   }

   public class OrbitalCustomTracker extends AuraTracker implements IParentSkill, Runnable {
      private float resolvedRadius;
      private float resolvedHitRadius;
      private float resolvedVerticalHitRadius;
      private int resolvedPoints;
      private int step;
      private double resolvedXRot;
      private double resolvedYRot;
      private double resolvedZRot;
      private double resolvedXOff;
      private double resolvedYOff;
      private double resolvedZOff;
      private boolean resolvedRotate;
      private double resolvedAvX;
      private double resolvedAvY;
      private double resolvedAvZ;
      private boolean resolvedReversed;
      private double resolvedRollRad;
      private boolean resolvedHitSelf;
      private boolean resolvedHitPlayers;
      private boolean resolvedHitNonPlayers;
      private Optional<Skill> resolvedOnHitSkill = Optional.empty();
      private OrbitalCustomMechanic.BulletType resolvedBulletType = OrbitalCustomMechanic.BulletType.NONE;
      private Material resolvedBulletMaterial = null;
      private int resolvedBulletModelId = 0;
      private MythicItem resolvedBulletMythicItem = null;
      private MythicMob resolvedBulletMob = null;
      private float resolvedBulletSpin = 0.0F;
      private AbstractLocation previousLocation;
      private AbstractEntity bullet = null;
      private final Set<AbstractEntity> inRange = ConcurrentHashMap.newKeySet();
      private final HashSet<AbstractEntity> targets = new HashSet<>();
      private final Map<AbstractEntity, Long> immune = new HashMap<>();

      public OrbitalCustomTracker(SkillMetadata data, AbstractEntity entity) {
         super(OrbitalCustomMechanic.this, entity, data);
         this.step = 0;
         this.start();
      }

      public OrbitalCustomTracker(SkillMetadata data, AbstractLocation location) {
         super(OrbitalCustomMechanic.this, location, data);
         this.step = 0;
         this.start();
      }

      private AbstractLocation getBaseLocation() {
         if (this.entity.isPresent()) {
            return ((AbstractEntity)this.entity.get()).getLocation().add(this.resolvedXOff, this.resolvedYOff, this.resolvedZOff);
         } else {
            return this.location.isPresent()
               ? ((AbstractLocation)this.location.get()).clone().add(this.resolvedXOff, this.resolvedYOff, this.resolvedZOff)
               : null;
         }
      }

      public void auraStart() {
         this.resolvedRadius = OrbitalCustomMechanic.this.radius.get(this.skillMetadata);
         this.resolvedHitRadius = OrbitalCustomMechanic.this.hitRadius.get(this.skillMetadata);
         if (OrbitalCustomMechanic.this.verticalHitRadius == null) {
            this.resolvedVerticalHitRadius = this.resolvedHitRadius;
         } else {
            this.resolvedVerticalHitRadius = OrbitalCustomMechanic.this.verticalHitRadius.get(this.skillMetadata);
         }

         this.resolvedPoints = Math.max(1, OrbitalCustomMechanic.this.points.get(this.skillMetadata));
         this.step = OrbitalCustomMechanic.this.startingStep.get(this.skillMetadata);
         this.resolvedXRot = OrbitalCustomMechanic.this.xRotation.get(this.skillMetadata);
         this.resolvedYRot = OrbitalCustomMechanic.this.yRotation.get(this.skillMetadata);
         this.resolvedZRot = OrbitalCustomMechanic.this.zRotation.get(this.skillMetadata);
         this.resolvedXOff = OrbitalCustomMechanic.this.xOffset.get(this.skillMetadata);
         this.resolvedYOff = OrbitalCustomMechanic.this.yOffset.get(this.skillMetadata);
         this.resolvedZOff = OrbitalCustomMechanic.this.zOffset.get(this.skillMetadata);
         double avxRaw = OrbitalCustomMechanic.this.angularVelocityX.get(this.skillMetadata);
         double avyRaw = OrbitalCustomMechanic.this.angularVelocityY.get(this.skillMetadata);
         double avzRaw = OrbitalCustomMechanic.this.angularVelocityZ.get(this.skillMetadata);
         this.resolvedAvX = avxRaw == 0.0 ? 0.0 : Math.PI / avxRaw;
         this.resolvedAvY = avyRaw == 0.0 ? 0.0 : Math.PI / avyRaw;
         this.resolvedAvZ = avzRaw == 0.0 ? 0.0 : Math.PI / avzRaw;
         if (OrbitalCustomMechanic.this.rotate) {
            this.resolvedRotate = OrbitalCustomMechanic.this.rotate;
         } else {
            this.resolvedRotate = this.resolvedAvX > 0.0 || this.resolvedAvY > 0.0 || this.resolvedAvZ > 0.0;
         }

         this.resolvedReversed = OrbitalCustomMechanic.this.reversed;
         double rollDeg = OrbitalCustomMechanic.this.rollDegrees.get(this.skillMetadata);
         rollDeg %= 360.0;
         if (rollDeg < 0.0) {
            rollDeg += 360.0;
         }

         this.resolvedRollRad = Math.toRadians(rollDeg);
         this.resolvedHitSelf = OrbitalCustomMechanic.this.hitSelf;
         this.resolvedHitPlayers = OrbitalCustomMechanic.this.hitPlayers;
         this.resolvedHitNonPlayers = OrbitalCustomMechanic.this.hitNonPlayers;
         String onHitName = OrbitalCustomMechanic.this.onHitSkillName;
         if (onHitName != null && !onHitName.trim().isEmpty()) {
            this.resolvedOnHitSkill = MythicBukkit.inst().getSkillManager().getSkill(onHitName.trim());
         }

         String bt = OrbitalCustomMechanic.this.bulletTypeName;
         this.resolvedBulletType = OrbitalCustomMechanic.parseBulletType(bt);
         this.resolvedBulletSpin = OrbitalCustomMechanic.this.bulletSpin;
         if (this.resolvedBulletType == OrbitalCustomMechanic.BulletType.BLOCK
            || this.resolvedBulletType == OrbitalCustomMechanic.BulletType.ITEM
            || this.resolvedBulletType == OrbitalCustomMechanic.BulletType.SMALLBLOCK) {
            this.resolvedBulletModelId = OrbitalCustomMechanic.this.bulletModelId;
            String matName = OrbitalCustomMechanic.this.bulletMaterialName;
            this.resolvedBulletMaterial = OrbitalCustomMechanic.parseMaterial(matName);
         } else if (this.resolvedBulletType == OrbitalCustomMechanic.BulletType.MYTHICITEM) {
            String miName = OrbitalCustomMechanic.this.bulletMythicItemName;
            if (miName != null && !miName.trim().isEmpty()) {
               Optional<MythicItem> maybe = MythicBukkit.inst().getItemManager().getItem(miName);
               maybe.ifPresent(item -> this.resolvedBulletMythicItem = item);
            }
         } else if (this.resolvedBulletType == OrbitalCustomMechanic.BulletType.MOB) {
            String mobType = OrbitalCustomMechanic.this.bulletMobTypeName;
            if (mobType != null && !mobType.trim().isEmpty()) {
               this.resolvedBulletMob = MythicBukkit.inst().getMobManager().getMythicMob(mobType.trim());
               if (this.resolvedBulletMob == null) {
                  MythicLogger.debug(DebugLevel.MECHANIC, "[orbitalCustom] invalid bullet mob type: " + mobType, new Object[0]);
                  this.resolvedBulletType = OrbitalCustomMechanic.BulletType.NONE;
               }
            } else {
               this.resolvedBulletType = OrbitalCustomMechanic.BulletType.NONE;
            }
         }

         if (this.resolvedHitSelf || this.resolvedHitPlayers || this.resolvedHitNonPlayers) {
            this.inRange.addAll(AbstractSkill.getPlugin().getEntityManager().getLivingEntities(this.skillMetadata.getOrigin().getWorld()));
            this.inRange.removeIf(e -> {
               if (e == null) {
                  return SkillResult.SUCCESS;
               } else if (!this.resolvedHitSelf && e.getUniqueId().equals(this.skillMetadata.getCaster().getEntity().getUniqueId())) {
                  return SkillResult.SUCCESS;
               } else {
                  return !this.resolvedHitPlayers && e.isPlayer() ? true : !this.resolvedHitNonPlayers && !e.isPlayer();
               }
            });
         }

         if (this.previousLocation == null) {
            this.previousLocation = this.getBaseLocation();
         }

         this.spawnBullet();
         this.executeAuraSkill(OrbitalCustomMechanic.this.onStartSkill, this.skillMetadata.deepClone().setOrigin(this.previousLocation));
      }

      public void auraTick() {
         if (this.previousLocation == null) {
            this.previousLocation = this.getBaseLocation();
         }

         AbstractLocation base = this.getBaseLocation();
         if (base != null) {
            double inc = (Math.PI * 2) / this.resolvedPoints;
            double baseAngle = (this.resolvedReversed ? -this.step : this.step) * inc;
            double angle = baseAngle + this.resolvedRollRad;
            AbstractVector v = new AbstractVector(0, 0, 0);
            v.setX(Math.cos(angle) * this.resolvedRadius);
            v.setZ(Math.sin(angle) * this.resolvedRadius);
            VectorUtils.rotateVector(v, this.resolvedXRot, this.resolvedYRot, this.resolvedZRot);
            if (this.resolvedRotate) {
               VectorUtils.rotateVector(v, this.resolvedAvX * this.step, this.resolvedAvY * this.step, this.resolvedAvZ * this.step);
            }

            AbstractLocation location = base.add(v);
            this.executeAuraSkill(OrbitalCustomMechanic.this.onTickSkill, this.skillMetadata.deepClone().setOrigin(location));
            Schedulers.sync().run(() -> {
               if (this.bullet != null) {
                  if (this.resolvedBulletType == OrbitalCustomMechanic.BulletType.ITEM) {
                     AbstractLocation ol = this.previousLocation.clone().subtract(0.0, 0.35, 0.0);
                     AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().setItemPosition(this.bullet, ol);
                     this.bullet.setVelocity(location.toVector().subtract(this.previousLocation.toVector()));
                  } else if (this.resolvedBulletType == OrbitalCustomMechanic.BulletType.ARROW) {
                     this.bullet.setVelocity(location.toVector().subtract(this.bullet.getLocation().clone().toVector()).multiply(0.25));
                  } else if (this.resolvedBulletType == OrbitalCustomMechanic.BulletType.MOB) {
                     AbstractLocation ol = this.previousLocation.clone().subtract(0.0, 1.35, 0.0);
                     if (this.resolvedBulletSpin != 0.0F) {
                        float newSpin = this.bullet.getLocation().getYaw() + this.resolvedBulletSpin;
                        ol.setYaw(newSpin);
                     }

                     this.bullet.teleport(ol);
                  } else {
                     this.bullet.setVelocity(location.toVector().subtract(this.bullet.getLocation().clone().toVector()).multiply(1));
                     if (this.resolvedBulletSpin > 0.0F) {
                        float newSpin = this.bullet.getLocation().getYaw() + this.resolvedBulletSpin;
                        AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().setEntityRotation(this.bullet, newSpin, newSpin);
                     }
                  }
               }
            });
            if (!this.inRange.isEmpty()) {
               ProjectileHitBox hitBox = new ProjectileHitBox(location, this.resolvedHitRadius, this.resolvedVerticalHitRadius);

               for (AbstractEntity e : this.inRange) {
                  if (!e.isDead() && hitBox.contains(e.getLocation().add(0.0, 0.6, 0.0))) {
                     this.targets.add(e);
                     this.immune.put(e, System.currentTimeMillis());
                     break;
                  }
               }

               this.immune.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis() - 2000L);
               if (!this.targets.isEmpty()) {
                  if (this.resolvedOnHitSkill.isPresent()) {
                     SkillMetadata sData = this.skillMetadata.deepClone();
                     sData.setEntityTargets((HashSet)this.targets.clone());
                     sData.setOrigin(location.clone());
                     Skill sk = this.resolvedOnHitSkill.get();
                     if (sk.isUsable(sData)) {
                        sk.execute(sData);
                     }
                  }

                  this.targets.clear();
                  this.consumeCharge();
               }
            }

            this.step++;
            this.previousLocation = location;
         }
      }

      public void auraStop() {
         this.executeAuraSkill(OrbitalCustomMechanic.this.onEndSkill, this.skillMetadata.deepClone().setOrigin(this.previousLocation));
         if (this.resolvedBulletType != OrbitalCustomMechanic.BulletType.NONE) {
            Schedulers.sync().runLater(() -> {
               if (this.bullet != null) {
                  this.bullet.remove();
                  OrbitalCustomMechanic.BULLET_ENTITIES.remove(this.bullet);
               }
            }, 2L);
         }
      }

      private void spawnBullet() {
         if (this.resolvedBulletType == OrbitalCustomMechanic.BulletType.BLOCK) {
            Schedulers.sync().run(() -> {
               if (!this.hasTerminated()) {
                  AbstractLocation l = this.getBaseLocation().clone().subtract(0.0, 0.5, 0.0);
                  FallingBlock block;
                  if (ServerVersion.isAfterOrEq(MinecraftVersions.v1_15)) {
                     block = BukkitAdapter.adapt(l).getWorld().spawnFallingBlock(BukkitAdapter.adapt(l), this.resolvedBulletMaterial.createBlockData());
                  } else {
                     block = BukkitAdapter.adapt(l).getWorld().spawnFallingBlock(BukkitAdapter.adapt(l), this.resolvedBulletMaterial, (byte)0);
                  }

                  block.setHurtEntities(false);
                  block.setDropItem(false);
                  block.setTicksLived(Integer.MAX_VALUE);
                  block.setInvulnerable(true);
                  block.setGravity(false);
                  this.bullet = BukkitAdapter.adapt(block);
                  OrbitalCustomMechanic.BULLET_ENTITIES.add(this.bullet);
                  AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().setHitBox(this.bullet, 0.0, 0.0, 0.0);
                  if (this.hasTerminated()) {
                     block.remove();
                  }
               }
            });
         } else if (this.resolvedBulletType == OrbitalCustomMechanic.BulletType.MYTHICITEM) {
            Schedulers.sync().run(() -> {
               if (this.resolvedBulletMythicItem != null && !this.hasTerminated()) {
                  ItemStack i = BukkitAdapter.adapt(this.resolvedBulletMythicItem.generateItemStack(1));
                  AbstractLocation l = this.getBaseLocation().clone().subtract(0.0, 0.35, 0.0);
                  Item item = BukkitAdapter.adapt(l).getWorld().dropItem(BukkitAdapter.adapt(l), i);
                  item.setTicksLived(Integer.MAX_VALUE);
                  item.setInvulnerable(true);
                  item.setGravity(false);
                  item.setPickupDelay(Integer.MAX_VALUE);
                  this.bullet = BukkitAdapter.adapt(item);
                  OrbitalCustomMechanic.BULLET_ENTITIES.add(this.bullet);
                  AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().setHitBox(this.bullet, 0.0, 0.0, 0.0);
                  if (this.hasTerminated()) {
                     item.remove();
                  }
               }
            });
         } else if (this.resolvedBulletType == OrbitalCustomMechanic.BulletType.SMALLBLOCK) {
            Schedulers.sync().run(() -> {
               if (!this.hasTerminated()) {
                  AbstractLocation l = this.getBaseLocation().clone();
                  ArmorStand as = (ArmorStand)BukkitAdapter.adapt(l).getWorld().spawnEntity(BukkitAdapter.adapt(l), EntityType.ARMOR_STAND);
                  as.setCustomName("Dinnerbone");
                  as.setCustomNameVisible(false);
                  as.setHeadPose(new EulerAngle(0.0, 0.0, 0.0));
                  as.getEquipment().setHelmet(new ItemStack(this.resolvedBulletMaterial));
                  as.setArms(false);
                  as.setBasePlate(false);
                  as.setVisible(false);
                  as.setTicksLived(Integer.MAX_VALUE);
                  as.setInvulnerable(true);
                  this.bullet = BukkitAdapter.adapt(as);
                  AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().setArmorStandNoGravity(this.bullet);
                  OrbitalCustomMechanic.BULLET_ENTITIES.add(this.bullet);
                  AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().setHitBox(this.bullet, 0.0, 0.0, 0.0);
                  if (this.hasTerminated()) {
                     as.remove();
                  }
               }
            });
         } else if (this.resolvedBulletType == OrbitalCustomMechanic.BulletType.ITEM) {
            Schedulers.sync().run(() -> {
               if (!this.hasTerminated()) {
                  ItemStack i = ItemFactory.of(this.resolvedBulletMaterial).model(this.resolvedBulletModelId).build();
                  AbstractLocation l = this.getBaseLocation().clone().subtract(0.0, 0.35, 0.0);
                  Item item = BukkitAdapter.adapt(l).getWorld().dropItem(BukkitAdapter.adapt(l), i);
                  item.setTicksLived(Integer.MAX_VALUE);
                  item.setInvulnerable(true);
                  item.setGravity(false);
                  item.setPickupDelay(Integer.MAX_VALUE);
                  this.bullet = BukkitAdapter.adapt(item);
                  OrbitalCustomMechanic.BULLET_ENTITIES.add(this.bullet);
                  AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().setItemPosition(this.bullet, l);
                  AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().sendEntityTeleportPacket(this.bullet);
                  AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().setHitBox(this.bullet, 0.0, 0.0, 0.0);
                  if (this.hasTerminated()) {
                     item.remove();
                  }
               }
            });
         } else if (this.resolvedBulletType == OrbitalCustomMechanic.BulletType.MOB && this.resolvedBulletType != null) {
            Schedulers.sync().runLater(() -> {
               if (!this.hasTerminated()) {
                  AbstractLocation l = this.previousLocation.clone().subtract(0.0, 1.35, 0.0);
                  ActiveMob am = this.resolvedBulletMob.spawn(l, 1.0, SpawnReason.OTHER);
                  Entity ent = am.getEntity().getBukkitEntity();
                  am.setParent(this.skillMetadata.getCaster());
                  am.setOwner(this.skillMetadata.getCaster().getEntity().getUniqueId());
                  ent.setTicksLived(Integer.MAX_VALUE);
                  ent.setInvulnerable(true);
                  this.bullet = BukkitAdapter.adapt(ent);
                  OrbitalCustomMechanic.BULLET_ENTITIES.add(this.bullet);
                  if (ent.getType().equals(EntityType.ARMOR_STAND)) {
                     AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().setArmorStandNoGravity(this.bullet);
                     this.bullet.setAI(true);
                     ((ArmorStand)ent).setRemoveWhenFarAway(true);
                  } else {
                     ent.setGravity(false);
                     if (ent instanceof LivingEntity) {
                        ((LivingEntity)ent).setRemoveWhenFarAway(true);
                     }
                  }

                  AbstractSkill.getPlugin().getVolatileCodeHandler().getEntityHandler().setHitBox(this.bullet, 0.0, 0.0, 0.0);
                  if (this.hasTerminated()) {
                     this.bullet.remove();
                  }
               }
            }, 2L);
         }
      }
   }
}
