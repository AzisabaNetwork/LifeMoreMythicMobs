package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.adapters.AbstractVector;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.logging.MythicLogger;
import io.lumine.mythic.api.skills.Skill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.mechanics.ProjectileMechanic;
import io.lumine.mythic.api.skills.projectiles.Projectile.BulletType;
import io.lumine.mythic.api.skills.projectiles.Projectile.ProjectileTracker;
import io.lumine.mythic.util.BlockUtil;
import io.lumine.mythic.util.MythicUtil;
import java.util.HashSet;
import java.util.Map.Entry;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class CustomProjectileMechanic extends ProjectileMechanic {
   private final String pathType;
   private final String sineAxis;
   private final float amplitude;
   private final float frequency;
   private final float zigzagangle;
   private final int zigzagPeriod;
   private final float spiralAmplitude;
   private final float spiralFrequency;

   public CustomProjectileMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.pathType = config.getString("path", "linear").toLowerCase();
      this.sineAxis = config.getString("axis", "y").toLowerCase();
      this.amplitude = config.getFloat("amplitude", 1.0F);
      this.frequency = config.getFloat("frequency", 0.1F);
      this.zigzagangle = config.getFloat("zigzagangle", 30.0F);
      this.zigzagPeriod = config.getInteger("zigzagperiod", 5);
      this.spiralAmplitude = config.getFloat("spiralamplitude", 1.0F);
      this.spiralFrequency = config.getFloat("spiralfrequency", 0.3F);
   }

   public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
      try {
         CustomProjectile projectile = new CustomProjectile(this, this.config, data, target);
         ProjectileTracker tracker = projectile.createCustomTracker(data, target);
         tracker.start();
         return SkillResult.SUCCESS;
      } catch (Exception ex) {
         MythicLogger.error("An error occurred executing CustomProjectileMechanic", ex);
         return SkillResult.FAILURE;
      }
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      return this.castAtLocation(data, target.getLocation().add(0.0, target.getEyeHeight() / 2.0, 0.0));
   }

   public ProjectileTracker createTracker(SkillMetadata data, AbstractLocation target) {
      return new CustomProjectileMechanic.CustomProjectileTracker(this, data, target);
   }

   private AbstractVector getPerpendicularAxis(AbstractVector param1, String param2) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.ClassCastException: class org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent cannot be cast to class org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent (org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent and org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent are in unnamed module of loader 'app')
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.findSyntheticDupVar(SwitchHelper.java:430)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper$StringSwitch.isValid(SwitchHelper.java:929)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper$Merged.match(SwitchHelper.java:1111)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper$StringSwitch.match(SwitchHelper.java:901)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.trySimplifyStringSwitch(SwitchHelper.java:221)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplify(SwitchHelper.java:210)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:30)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:410)
      //
      // Bytecode:
      // 00: new org/bukkit/util/Vector
      // 03: dup
      // 04: aload 1
      // 05: invokevirtual io/lumine/xikage/mythicmobs/adapters/AbstractVector.getX ()D
      // 08: aload 1
      // 09: invokevirtual io/lumine/xikage/mythicmobs/adapters/AbstractVector.getY ()D
      // 0c: aload 1
      // 0d: invokevirtual io/lumine/xikage/mythicmobs/adapters/AbstractVector.getZ ()D
      // 10: invokespecial org/bukkit/util/Vector.<init> (DDD)V
      // 13: astore 3
      // 14: aload 2
      // 15: invokevirtual java/lang/String.toLowerCase ()Ljava/lang/String;
      // 18: dup
      // 19: astore 4
      // 1b: invokevirtual java/lang/String.hashCode ()I
      // 1e: lookupswitch 126 3 120 34 121 47 122 60
      // 40: aload 4
      // 42: ldc "x"
      // 44: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 47: ifne 9c
      // 4a: goto 9c
      // 4d: aload 4
      // 4f: ldc "y"
      // 51: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 54: ifne 67
      // 57: goto 9c
      // 5a: aload 4
      // 5c: ldc "z"
      // 5e: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 61: ifne 9c
      // 64: goto 9c
      // 67: aload 3
      // 68: invokevirtual org/bukkit/util/Vector.clone ()Lorg/bukkit/util/Vector;
      // 6b: bipush 0
      // 6c: invokevirtual org/bukkit/util/Vector.setY (I)Lorg/bukkit/util/Vector;
      // 6f: invokevirtual org/bukkit/util/Vector.normalize ()Lorg/bukkit/util/Vector;
      // 72: astore 5
      // 74: aload 5
      // 76: invokevirtual org/bukkit/util/Vector.clone ()Lorg/bukkit/util/Vector;
      // 79: aload 3
      // 7a: invokevirtual org/bukkit/util/Vector.crossProduct (Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;
      // 7d: invokevirtual org/bukkit/util/Vector.normalize ()Lorg/bukkit/util/Vector;
      // 80: astore 6
      // 82: new io/lumine/xikage/mythicmobs/adapters/AbstractVector
      // 85: dup
      // 86: aload 6
      // 88: invokevirtual org/bukkit/util/Vector.getX ()D
      // 8b: d2f
      // 8c: aload 6
      // 8e: invokevirtual org/bukkit/util/Vector.getY ()D
      // 91: d2f
      // 92: aload 6
      // 94: invokevirtual org/bukkit/util/Vector.getZ ()D
      // 97: d2f
      // 98: invokespecial io/lumine/xikage/mythicmobs/adapters/AbstractVector.<init> (FFF)V
      // 9b: areturn
      // 9c: aload 3
      // 9d: invokevirtual org/bukkit/util/Vector.clone ()Lorg/bukkit/util/Vector;
      // a0: new org/bukkit/util/Vector
      // a3: dup
      // a4: bipush 0
      // a5: bipush 1
      // a6: bipush 0
      // a7: invokespecial org/bukkit/util/Vector.<init> (III)V
      // aa: invokevirtual org/bukkit/util/Vector.crossProduct (Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;
      // ad: astore 5
      // af: aload 3
      // b0: invokevirtual org/bukkit/util/Vector.clone ()Lorg/bukkit/util/Vector;
      // b3: aload 5
      // b5: invokevirtual org/bukkit/util/Vector.crossProduct (Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;
      // b8: invokevirtual org/bukkit/util/Vector.normalize ()Lorg/bukkit/util/Vector;
      // bb: astore 6
      // bd: new io/lumine/xikage/mythicmobs/adapters/AbstractVector
      // c0: dup
      // c1: aload 6
      // c3: invokevirtual org/bukkit/util/Vector.getX ()D
      // c6: d2f
      // c7: aload 6
      // c9: invokevirtual org/bukkit/util/Vector.getY ()D
      // cc: d2f
      // cd: aload 6
      // cf: invokevirtual org/bukkit/util/Vector.getZ ()D
      // d2: d2f
      // d3: invokespecial io/lumine/xikage/mythicmobs/adapters/AbstractVector.<init> (FFF)V
      // d6: areturn
   }

   private Vector rotateAroundAxis(Vector vec, Vector axis, float angleRad) {
      double cos = Math.cos(angleRad);
      double sin = Math.sin(angleRad);
      double dot = vec.dot(axis);
      Vector part1 = vec.clone().multiply(cos);
      Vector part2 = axis.clone().crossProduct(vec).multiply(sin);
      Vector part3 = axis.clone().multiply(dot * (1.0 - cos));
      return part1.add(part2).add(part3);
   }

   private Vector getZigzagRotationAxis(Vector param1, String param2) {
      // $VF: Couldn't be decompiled
      // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
      // java.lang.ClassCastException: class org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent cannot be cast to class org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent (org.jetbrains.java.decompiler.modules.decompiler.exps.AssignmentExprent and org.jetbrains.java.decompiler.modules.decompiler.exps.VarExprent are in unnamed module of loader 'app')
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.findSyntheticDupVar(SwitchHelper.java:430)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper$StringSwitch.isValid(SwitchHelper.java:929)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper$Merged.match(SwitchHelper.java:1111)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper$StringSwitch.match(SwitchHelper.java:901)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.trySimplifyStringSwitch(SwitchHelper.java:221)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplify(SwitchHelper.java:210)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:30)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.modules.decompiler.SwitchHelper.simplifySwitches(SwitchHelper.java:34)
      //   at org.jetbrains.java.decompiler.main.rels.MethodProcessor.codeToJava(MethodProcessor.java:410)
      //
      // Bytecode:
      // 00: aload 1
      // 01: invokevirtual org/bukkit/util/Vector.clone ()Lorg/bukkit/util/Vector;
      // 04: invokevirtual org/bukkit/util/Vector.normalize ()Lorg/bukkit/util/Vector;
      // 07: astore 3
      // 08: aload 2
      // 09: invokevirtual java/lang/String.toLowerCase ()Ljava/lang/String;
      // 0c: dup
      // 0d: astore 4
      // 0f: invokevirtual java/lang/String.hashCode ()I
      // 12: lookupswitch 132 3 120 34 121 47 122 60
      // 34: aload 4
      // 36: ldc "x"
      // 38: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 3b: ifne 5b
      // 3e: goto 96
      // 41: aload 4
      // 43: ldc "y"
      // 45: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 48: ifne 96
      // 4b: goto 96
      // 4e: aload 4
      // 50: ldc "z"
      // 52: invokevirtual java/lang/String.equals (Ljava/lang/Object;)Z
      // 55: ifne 5b
      // 58: goto 96
      // 5b: aload 3
      // 5c: invokevirtual org/bukkit/util/Vector.clone ()Lorg/bukkit/util/Vector;
      // 5f: bipush 0
      // 60: invokevirtual org/bukkit/util/Vector.setY (I)Lorg/bukkit/util/Vector;
      // 63: astore 5
      // 65: aload 5
      // 67: invokevirtual org/bukkit/util/Vector.lengthSquared ()D
      // 6a: ldc2_w 1.0E-6
      // 6d: dcmpg
      // 6e: ifge 7c
      // 71: new org/bukkit/util/Vector
      // 74: dup
      // 75: bipush 1
      // 76: bipush 0
      // 77: bipush 0
      // 78: invokespecial org/bukkit/util/Vector.<init> (III)V
      // 7b: areturn
      // 7c: new org/bukkit/util/Vector
      // 7f: dup
      // 80: bipush 0
      // 81: bipush 1
      // 82: bipush 0
      // 83: invokespecial org/bukkit/util/Vector.<init> (III)V
      // 86: astore 6
      // 88: aload 6
      // 8a: invokevirtual org/bukkit/util/Vector.clone ()Lorg/bukkit/util/Vector;
      // 8d: aload 5
      // 8f: invokevirtual org/bukkit/util/Vector.crossProduct (Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;
      // 92: invokevirtual org/bukkit/util/Vector.normalize ()Lorg/bukkit/util/Vector;
      // 95: areturn
      // 96: aload 1
      // 97: invokevirtual org/bukkit/util/Vector.clone ()Lorg/bukkit/util/Vector;
      // 9a: new org/bukkit/util/Vector
      // 9d: dup
      // 9e: bipush 0
      // 9f: bipush 1
      // a0: bipush 0
      // a1: invokespecial org/bukkit/util/Vector.<init> (III)V
      // a4: invokevirtual org/bukkit/util/Vector.crossProduct (Lorg/bukkit/util/Vector;)Lorg/bukkit/util/Vector;
      // a7: invokevirtual org/bukkit/util/Vector.normalize ()Lorg/bukkit/util/Vector;
      // aa: areturn
   }

   private Vector getPerpendicularVibrationAxis(Vector forward) {
      Vector fixed = new Vector(0, 0, 1);
      if (Math.abs(forward.dot(fixed)) > 0.99) {
         fixed = new Vector(0, 1, 0);
      }

      return forward.clone().crossProduct(fixed).normalize();
   }

   public class CustomProjectileTracker extends ProjectileTracker {
      private int tickCount = 0;
      private AbstractVector baseDirection;
      private final CustomProjectileMechanic mechanic;
      private int lastBlockX = Integer.MIN_VALUE;
      private int lastBlockZ = Integer.MIN_VALUE;

      public CustomProjectileTracker(CustomProjectileMechanic mechanic, SkillMetadata data, AbstractLocation target) {
         super(CustomProjectileMechanic.this, data, target);
         this.mechanic = mechanic;
      }

      public void projectileStart() {
         if (CustomProjectileMechanic.this.sourceIsOrigin) {
            this.startLocation = this.data.getOrigin().clone();
         } else {
            this.startLocation = this.data.getCaster().getEntity().getLocation().clone();
         }

         if (CustomProjectileMechanic.this.startYOffset != 0.0F) {
            this.startLocation.setY(this.startLocation.getY() + CustomProjectileMechanic.this.startYOffset);
         }

         if (CustomProjectileMechanic.this.startForwardOffset != 0.0F) {
            this.startLocation = MythicUtil.move(this.startLocation, CustomProjectileMechanic.this.startForwardOffset, 0.0, 0.0);
         }

         if (CustomProjectileMechanic.this.startSideOffset != 0.0F) {
            this.startLocation = MythicUtil.move(this.startLocation, 0.0, 0.0, CustomProjectileMechanic.this.startSideOffset);
         }

         this.previousLocation = this.startLocation.clone();
         this.currentLocation = this.startLocation.clone();
         AbstractVector direction = this.startLocation.getDirection().normalize();
         this.currentVelocity = new AbstractVector((float)direction.getX(), (float)direction.getY(), (float)direction.getZ())
            .multiply(CustomProjectileMechanic.this.projectileVelocity / CustomProjectileMechanic.this.ticksPerSecond);
         this.baseDirection = this.currentVelocity.clone().normalize();
      }

      public void projectileTick() {
         this.previousLocation = this.currentLocation.clone();
         this.tickCount++;
         AbstractVector offset = new AbstractVector(0, 0, 0);
         if (this.mechanic.pathType.equals("sine")) {
            double frequency = this.mechanic.frequency;
            double amplitude = this.mechanic.amplitude;
            double moveStep = CustomProjectileMechanic.this.projectileVelocity / CustomProjectileMechanic.this.ticksPerSecond;
            Vector forward = new Vector(this.baseDirection.getX(), this.baseDirection.getY(), this.baseDirection.getZ());
            Vector move = forward.clone().multiply(moveStep);
            double time = (double)this.tickCount / CustomProjectileMechanic.this.ticksPerSecond;
            double sinValue = Math.sin(time * frequency * 2.0 * Math.PI);
            double sineOffset = amplitude * sinValue;
            AbstractVector axisVector = CustomProjectileMechanic.this.getPerpendicularAxis(this.baseDirection, this.mechanic.sineAxis);
            Vector offsetDirection = new Vector(axisVector.getX(), axisVector.getY(), axisVector.getZ()).normalize();
            offsetDirection = CustomProjectileMechanic.this.rotateAroundAxis(offsetDirection, forward, (float)Math.toRadians(90.0));
            if (this.mechanic.sineAxis.equals("z")) {
               offsetDirection.multiply(-1);
            }

            Vector offsetVec = offsetDirection.multiply(sineOffset);
            Vector combined = move.clone().add(offsetVec);
            offset = new AbstractVector((float)combined.getX(), (float)combined.getY(), (float)combined.getZ());
         } else if (this.mechanic.pathType.equals("zigzag")) {
            float safeAngle = Math.min(this.mechanic.zigzagangle, 180.0F);
            float angleRad = (float)Math.toRadians(safeAngle);
            double moveStep = CustomProjectileMechanic.this.projectileVelocity / CustomProjectileMechanic.this.ticksPerSecond;
            Vector forward = new Vector(this.baseDirection.getX(), this.baseDirection.getY(), this.baseDirection.getZ()).normalize();
            AbstractVector axisVector = CustomProjectileMechanic.this.getPerpendicularAxis(this.baseDirection, this.mechanic.sineAxis);
            Vector vibrationAxis = new Vector(axisVector.getX(), axisVector.getY(), axisVector.getZ());
            int phase = (this.tickCount + CustomProjectileMechanic.this.zigzagPeriod / 2) / CustomProjectileMechanic.this.zigzagPeriod % 2;
            float actualAngleRad = (phase == 0 ? 1 : -1) * angleRad;
            if (this.mechanic.sineAxis.equals("z")) {
               actualAngleRad *= -1.0F;
            }

            Vector rotated = CustomProjectileMechanic.this.rotateAroundAxis(forward, vibrationAxis, actualAngleRad);
            Vector move = rotated.multiply(moveStep);
            offset = new AbstractVector((float)move.getX(), (float)move.getY(), (float)move.getZ());
         } else if (this.mechanic.pathType.equals("spiral")) {
            Vector forward = new Vector(this.baseDirection.getX(), this.baseDirection.getY(), this.baseDirection.getZ()).normalize();
            Vector up = new Vector(0, 1, 0);
            if (Math.abs(forward.dot(up)) > 0.99) {
               up = new Vector(1, 0, 0);
            }

            Vector orthogonal1 = forward.clone().getCrossProduct(up).normalize();
            Vector orthogonal2 = forward.clone().getCrossProduct(orthogonal1).normalize();
            double angle = this.tickCount * this.mechanic.spiralFrequency;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);
            Vector spiralOffset = orthogonal1.multiply(cos).add(orthogonal2.multiply(sin)).multiply(this.mechanic.spiralAmplitude);
            Vector move = forward.clone().multiply(CustomProjectileMechanic.this.projectileVelocity / CustomProjectileMechanic.this.ticksPerSecond);
            Vector finalOffset = move.clone().add(spiralOffset);
            offset = new AbstractVector((float)finalOffset.getX(), (float)finalOffset.getY(), (float)finalOffset.getZ());
         }

         this.currentLocation
            .add(
               this.baseDirection.clone().multiply(CustomProjectileMechanic.this.projectileVelocity / CustomProjectileMechanic.this.ticksPerSecond).add(offset)
            );
         if (this.mechanic.hugSurface) {
            int blockX = this.currentLocation.getBlockX();
            int blockZ = this.currentLocation.getBlockZ();
            if (blockX != this.lastBlockX || blockZ != this.lastBlockZ) {
               Block b = BukkitAdapter.adapt(this.currentLocation.subtract(0.0, this.mechanic.heightFromSurface, 0.0)).getBlock();
               if (BlockUtil.isPathable(b, this)) {
                  int attempts = 0;
                  boolean ok = false;

                  while (attempts++ < 10) {
                     b = b.getRelative(BlockFace.DOWN);
                     if (!BlockUtil.isPathable(b, this)) {
                        ok = true;
                        break;
                     }

                     this.currentLocation.add(0.0, -1.0, 0.0);
                  }

                  if (!ok) {
                     this.terminate();
                     return;
                  }
               } else {
                  int attempts = 0;
                  boolean ok = false;

                  while (attempts++ < 10) {
                     b = b.getRelative(BlockFace.UP);
                     this.currentLocation.add(0.0, 1.0, 0.0);
                     if (BlockUtil.isPathable(b)) {
                        ok = true;
                        break;
                     }
                  }

                  if (!ok) {
                     this.terminate();
                     return;
                  }
               }

               this.currentLocation.setY((int)this.currentLocation.getY() + this.mechanic.heightFromSurface);
               this.lastBlockX = blockX;
               this.lastBlockZ = blockZ;
            }
         } else if (this.mechanic.projectileGravity != 0.0F) {
            this.currentVelocity.setY(this.currentVelocity.getY() - this.mechanic.projectileGravity / this.mechanic.ticksPerSecond);
         }

         if (this.mechanic.stopOnHitGround && !BlockUtil.isPathable(BukkitAdapter.adapt(this.currentLocation).getBlock(), this)) {
            this.currentLocation = this.previousLocation;
            this.terminate();
         } else {
            if (this.bullet != null) {
               this.applyBulletVelocity();
            }

            if (this.inRange != null && !this.inRange.isEmpty()) {
               this.immune.entrySet().removeIf(entry -> (Long)entry.getValue() < System.currentTimeMillis() - 2000L);

               for (AbstractEntity e : this.inRange) {
                  if (!e.isDead() && this.getBoundingBox().overlaps(e.getBukkitEntity().getBoundingBox()) && !this.immune.containsKey(e)) {
                     this.targets.add(e);
                     this.immune.put(e, System.currentTimeMillis());
                     break;
                  }
               }
            }

            if (this.mechanic.onTickSkill.isPresent() && ((Skill)this.mechanic.onTickSkill.get()).isUsable(this.data)) {
               SkillMetadata sData = this.data.deepClone();
               AbstractLocation location = this.mechanic.bulletType == BulletType.ARROW ? this.previousLocation.clone() : this.currentLocation.clone();
               HashSet<AbstractLocation> targets = new HashSet<>();
               targets.add(location);
               sData.setLocationTargets(targets);
               sData.setOrigin(location);
               ((Skill)this.mechanic.onTickSkill.get()).execute(sData);
            }

            if (!this.targets.isEmpty()) {
               if (CustomProjectileMechanic.this.onHitSkill.isPresent()) {
                  SkillMetadata sData = this.data.deepClone();
                  sData.setEntityTargets(new HashSet(this.targets));
                  sData.setOrigin(this.currentLocation.clone());
                  ((Skill)CustomProjectileMechanic.this.onHitSkill.get()).execute(sData);
               }

               if (CustomProjectileMechanic.this.stopOnHitEntity) {
                  this.terminate();
               }
            }

            this.targets.clear();
         }
      }

      public void applyBulletVelocity() {
      }
   }
}
