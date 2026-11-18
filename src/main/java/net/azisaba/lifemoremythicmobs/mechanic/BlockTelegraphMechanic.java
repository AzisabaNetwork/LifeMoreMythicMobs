package net.azisaba.lifemoremythicmobs.mechanic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.core.skills.SkillExecutor;
import io.lumine.mythic.core.skills.SkillMechanic;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * 地面に置くブロックディスプレイの予兆（2026-11-15: 粒子の予兆は小さくて見づらい）。
 * <pre>
 * bdTele{s=circle;r=5;d=30} @selflocation           円: 縁（c）＋中心から d tick で広がる面（f）
 * bdTele{s=donut;r=12;ri=4;d=30} @selflocation      輪: 外と内の縁＋外から内へ縮む縁（内側は安全）
 * bdTele{s=rect;l=20;w=3;rot=0;d=24} @selflocation  帯: 唱えた者の向き＋rot 度。手前から奥へ伸びる面
 * bdTele{s=sector;r=9;a=90;rot=0;d=20} @selflocation 扇
 * bdTele{s=ring;r=0.5;r2=14;d=20} @selflocation     動く縁（衝撃波は広がる・閉じる輪は縮む）
 * </pre>
 * c: 縁のブロック（既定 red_concrete）、f: 面のブロック（既定 red_stained_glass。none で面なし）、grow=false で面は最初から全部。
 * d tick で縁が白く光り、4 tick 後に消える（flash=false なら光らずに消える）。向き: @Forward{rot=a} と同じ（唱えた者の向き + a 度）。
 */
public class BlockTelegraphMechanic extends SkillMechanic implements ITargetedLocationSkill, ITargetedEntitySkill {
    private static final float H = 0.03f;       // 板の厚み
    private static final float T = 0.22f;       // 縁の太さ
    private final String shape;
    private final float r, ri, r2, l, w, a, rot, y;
    private final int d;
    private final BlockData edge, fill, outline;
    private static final float O = 0.16f;      // 外側の暗い縁の太さ
    private final boolean grow, flash;
    /** 光らせる色（攻撃中の物。予兆と見分ける）。null なら光らない */
    private final org.bukkit.Color glow;

    public BlockTelegraphMechanic(SkillExecutor executor, MythicLineConfig config) {
        super(executor, config.getLine(), config);
        this.shape = config.getString(new String[] {"shape", "s"}, "circle").toLowerCase();
        this.r = (float) config.getDouble(new String[] {"radius", "r"}, 3);
        this.ri = (float) config.getDouble(new String[] {"inner", "ri"}, 0);
        this.r2 = (float) config.getDouble(new String[] {"radius2", "r2"}, 0);
        this.l = (float) config.getDouble(new String[] {"length", "l"}, 10);
        this.w = (float) config.getDouble(new String[] {"width", "w"}, 2);
        this.a = (float) config.getDouble(new String[] {"angle", "a"}, 90);
        this.rot = (float) config.getDouble(new String[] {"rotation", "rot"}, 0);
        this.y = (float) config.getDouble(new String[] {"yoffset", "y"}, 0.06);
        this.d = Math.max(1, config.getInteger(new String[] {"duration", "d"}, 20));
        this.edge = block(config.getString(new String[] {"edge", "c"}, "red_concrete"));
        // 2026-11-15: 明るい地面（塩湖・雪）でも縁が埋もれないよう、外側に暗い縁（o=none で付けない）
        String o = config.getString(new String[] {"outline", "o"}, "black_concrete");
        this.outline = o.equalsIgnoreCase("none") ? null : block(o);
        String f = config.getString(new String[] {"fill", "f"}, "red_stained_glass");
        this.fill = f.equalsIgnoreCase("none") ? null : block(f);
        this.grow = config.getBoolean(new String[] {"grow", "g"}, true);
        this.flash = config.getBoolean(new String[] {"flash", "fl"}, true);
        // 2026-11-15: 攻撃中の物（今当たる）は光る縁取り。予兆（光らない板）と見分ける
        String gl = config.getString(new String[] {"glow", "gl"}, "");
        org.bukkit.Color gc = null;
        try {
            String h = gl.replace("#", "").replace("\"", "");
            if (h.length() == 6) gc = org.bukkit.Color.fromRGB(Integer.parseInt(h, 16));
        } catch (NumberFormatException ignored) {
        }
        this.glow = gc;
    }

    /** 真下の地面に下ろす（2026-11-15: 跳んでいる人の足元・ボスの目の高さに出した予兆が宙に浮いた）。埋まっていれば上へ */
    private static Location ground(Location l) {
        World w = l.getWorld();
        if (w == null) return l;
        int x = l.getBlockX(), z = l.getBlockZ(), y0 = l.getBlockY();
        for (int up = 0; up < 4 && !w.getBlockAt(x, y0, z).isPassable(); up++) y0++;
        for (int dn = 0; dn < 32; dn++) {
            if (!w.getBlockAt(x, y0 - 1, z).isPassable()) break;
            y0--;
        }
        Location out = l.clone();
        out.setY(y0);
        return out;
    }

    private static BlockData block(String name) {
        Material m = Material.matchMaterial(name);
        return (m == null || !m.isBlock() ? Material.RED_CONCRETE : m).createBlockData();
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        return castAtLocation(data, target.getLocation());
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        Location at = ground(BukkitAdapter.adapt(target).clone()).add(0, y, 0);
        float yaw = data.getCaster().getLocation().getYaw() + rot;
        if (Bukkit.isPrimaryThread()) draw(at, yaw);
        else Bukkit.getScheduler().runTask(LifeMoreMythicMobs.inst(), () -> draw(at, yaw));
        return SkillResult.SUCCESS;
    }

    // ------------------------------------------------------------------ 描く
    private final class Box {
        final BlockDisplay e;
        Transformation to;   // 補間で向かう形（null なら動かない）

        Box(BlockDisplay e) {
            this.e = e;
        }
    }

    /** 中心 at・向き yaw（度）のディスプレイに、ディスプレイの座標で (tx,tz) から大きさ (sx,sz) の板 */
    private BlockDisplay box(World world, Location at, float yaw, BlockData b, float tx, float ty, float tz, float sx, float sy, float sz) {
        Location l0 = at.clone();
        l0.setYaw(yaw);
        l0.setPitch(0);
        return world.spawn(l0, BlockDisplay.class, e -> {
            e.setBlock(b);
            e.setBrightness(new Display.Brightness(15, 15));
            e.setPersistent(false);
            e.setViewRange(2.5f);
            e.setTransformation(tf(tx, ty, tz, sx, sy, sz));
            if (glow != null) {
                e.setGlowing(true);
                e.setGlowColorOverride(glow);
            }
        });
    }

    private static Transformation tf(float tx, float ty, float tz, float sx, float sy, float sz) {
        return new Transformation(new Vector3f(tx, ty, tz), new AxisAngle4f(), new Vector3f(sx, sy, sz), new AxisAngle4f());
    }

    private void ringSegs(World world, Location at, float radius, List<BlockDisplay> edges, float from, float to) {
        int n = Math.max(12, Math.min(56, Math.round(radius * 4)));
        float len = (float) (2 * Math.PI * radius / n * 1.08);
        for (int k = 0; k < n; k++) {
            float th = from + (to - from) * (k + 0.5f) / n;
            edges.add(box(world, at, th, edge, -len / 2, 0, radius - T / 2, len, H * 1.5f, T));
            if (outline != null) edges.add(box(world, at, th, outline, -len * 1.04f / 2, -0.005f, radius + T / 2, len * 1.04f, H * 1.4f, O));
        }
    }

    private void draw(Location at, float yaw) {
        World world = at.getWorld();
        if (world == null) return;
        List<BlockDisplay> edges = new ArrayList<>();
        List<Box> moving = new ArrayList<>();
        switch (shape) {
            case "circle" -> {
                ringSegs(world, at, r, edges, 0, 360);
                if (fill != null) {
                    float s = r * 2 * 0.96f;
                    for (float th : new float[] {0, 45}) {
                        float s0 = grow ? 0.05f : s;
                        Box bx = new Box(box(world, at, th, fill, -s0 / 2, 0, -s0 / 2, s0, H, s0));
                        if (grow) bx.to = tf(-s / 2, 0, -s / 2, s, H, s);
                        moving.add(bx);
                    }
                }
            }
            case "donut" -> {
                ringSegs(world, at, r, edges, 0, 360);
                if (ri > 0.3f) ringSegs(world, at, ri, edges, 0, 360);
                movingRing(world, at, r, ri, moving);
            }
            case "ring" -> movingRing(world, at, r, r2, moving);
            case "rect" -> {
                float h = w / 2;
                edges.add(box(world, at, yaw, edge, h - T, 0, 0, T, H * 1.5f, l));
                edges.add(box(world, at, yaw, edge, -h, 0, 0, T, H * 1.5f, l));
                edges.add(box(world, at, yaw, edge, -h, 0, l - T, w, H * 1.5f, T));
                if (outline != null) {
                    edges.add(box(world, at, yaw, outline, h, -0.005f, 0, O, H * 1.4f, l + O));
                    edges.add(box(world, at, yaw, outline, -h - O, -0.005f, 0, O, H * 1.4f, l + O));
                    edges.add(box(world, at, yaw, outline, -h - O, -0.005f, l, w + 2 * O, H * 1.4f, O));
                }
                if (fill != null) {
                    Box bx = new Box(box(world, at, yaw, fill, -h, 0, 0, w, H, grow ? 0.05f : l));
                    if (grow) bx.to = tf(-h, 0, 0, w, H, l);
                    moving.add(bx);
                }
            }
            case "sector" -> {
                edges.add(box(world, at, yaw - a / 2, edge, -T / 2, 0, 0, T, H * 1.5f, r));
                edges.add(box(world, at, yaw + a / 2, edge, -T / 2, 0, 0, T, H * 1.5f, r));
                // 弧（MC の yaw は時計回り。向き yaw ± a/2 の間）
                int n = Math.max(6, Math.round(a / 360f * Math.max(12, r * 4)));
                float len = (float) (2 * Math.PI * r * (a / 360f) / n * 1.08);
                for (int k = 0; k < n; k++) {
                    float th = yaw - a / 2 + a * (k + 0.5f) / n;
                    edges.add(box(world, at, th, edge, -len / 2, 0, r - T, len, H * 1.5f, T));
                    if (outline != null) edges.add(box(world, at, th, outline, -len * 1.04f / 2, -0.005f, r, len * 1.04f, H * 1.4f, O));
                }
                if (fill != null) {
                    int m = Math.max(4, Math.round(a / 7));
                    float sw = (float) (2 * r * Math.sin(Math.toRadians(a / m / 2)) * 1.15);
                    for (int k = 0; k < m; k++) {
                        float th = yaw - a / 2 + a * (k + 0.5f) / m;
                        Box bx = new Box(box(world, at, th, fill, -sw / 2, 0, 0, sw, H, grow ? 0.05f : r));
                        if (grow) bx.to = tf(-sw / 2, 0, 0, sw, H, r);
                        moving.add(bx);
                    }
                }
            }
            default -> ringSegs(world, at, r, edges, 0, 360);
        }
        // 次の tick に補間を始める（出した tick に形を変えると補間されない）
        Bukkit.getScheduler().runTaskLater(LifeMoreMythicMobs.inst(), () -> {
            for (Box bx : moving) {
                if (bx.to == null || !bx.e.isValid()) continue;
                bx.e.setInterpolationDelay(0);
                bx.e.setInterpolationDuration(Math.max(1, d - 1));
                bx.e.setTransformation(bx.to);
            }
        }, 1L);
        // 当たる瞬間: 縁を白く光らせて消す
        Bukkit.getScheduler().runTaskLater(LifeMoreMythicMobs.inst(), () -> {
            if (!flash) {
                clear(edges, moving);
                return;
            }
            BlockData white = Material.WHITE_CONCRETE.createBlockData();
            for (BlockDisplay e : edges) if (e.isValid()) e.setBlock(white);
            for (Box bx : moving) if (bx.e.isValid()) bx.e.setBlock(white);
            Bukkit.getScheduler().runTaskLater(LifeMoreMythicMobs.inst(), () -> clear(edges, moving), 4L);
        }, d);
    }

    private void movingRing(World world, Location at, float from, float to, List<Box> moving) {
        int n = Math.max(12, Math.min(56, Math.round(Math.max(from, to) * 4)));
        for (int k = 0; k < n; k++) {
            float th = 360f * (k + 0.5f) / n;
            float l0 = (float) (2 * Math.PI * Math.max(0.3, from) / n * 1.08), l1 = (float) (2 * Math.PI * Math.max(0.3, to) / n * 1.08);
            BlockData b = fill != null ? fill : edge;
            Box bx = new Box(box(world, at, th, b, -l0 / 2, 0.01f, from - T, l0, H * 2, T * 1.4f));
            bx.to = tf(-l1 / 2, 0.01f, to - T, l1, H * 2, T * 1.4f);
            moving.add(bx);
        }
    }

    private static void clear(List<BlockDisplay> edges, List<Box> moving) {
        for (BlockDisplay e : edges) if (e.isValid()) e.remove();
        for (Box bx : moving) if (bx.e.isValid()) bx.e.remove();
    }
}
