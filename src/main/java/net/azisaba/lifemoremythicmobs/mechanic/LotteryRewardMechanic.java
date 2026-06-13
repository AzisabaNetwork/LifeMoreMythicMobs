package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class LotteryRewardMechanic extends SkillMechanic implements ITargetedEntitySkill {
   private final PlaceholderString winning;
   private final PlaceholderString count;
   private final PlaceholderString unit;
   private final PlaceholderString command;
   private final PlaceholderString yearmonth;

   public LotteryRewardMechanic(SkillExecutor executor, MythicLineConfig config) { super(executor, config.getLine(), config);
      this.winning = config.getPlaceholderString("winning", "200");
      this.count = config.getPlaceholderString("count", "200");
      this.unit = config.getPlaceholderString("unit", "200");
      this.command = config.getPlaceholderString("command", "");
      this.yearmonth = config.getPlaceholderString("yearmonth", "");
   }

   public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
      if (!target.isPlayer()) {
         return SkillResult.FAILURE;
      }

      Player player = (Player)target.getBukkitEntity();
      IgaDebugLogger.log(this.getClass(), "=== 抽選処理開始 ===");
      IgaDebugLogger.log(this.getClass(), "プレイヤー: " + player.getName());
      int winNum = Integer.parseInt(this.winning.get(data));
      int ticketCount = Integer.parseInt(this.count.get(data));
      int unitPrice = Integer.parseInt(this.unit.get(data));
      String ym = this.yearmonth.get(data);
      IgaDebugLogger.log(this.getClass(), "当選番号: " + winNum);
      IgaDebugLogger.log(this.getClass(), "枚数: " + ticketCount + ", 単価: " + unitPrice + ", 対象年月: " + ym);
      long totalSales = (long)ticketCount * unitPrice;
      long reward1 = totalSales * 30L / 100L;
      long rewardFrontRear = totalSales * 5L / 100L;
      long reward2 = totalSales * 10L / 100L;
      long reward3 = totalSales * 10L / 100L;
      long reward4 = totalSales * 10L / 100L;
      long reward5 = totalSales * 10L / 100L;
      long reward6 = totalSales * 10L / 100L;
      Set<Integer> pool = new HashSet<>();

      for (int i = 0; i < ticketCount; i++) {
         pool.add(i);
      }

      pool.remove(winNum);
      pool.remove(winNum - 1);
      pool.remove(winNum + 1);
      int count2 = this.countMatches(pool, winNum, 5);
      int count3 = this.countMatches(pool, winNum, 4);
      int count4 = this.countMatches(pool, winNum, 3);
      int count5 = this.countMatches(pool, winNum, 2);
      int count6 = this.countMatches(pool, winNum, 1);
      IgaDebugLogger.log(this.getClass(), "当選本数: 2等=" + count2 + ", 3等=" + count3 + ", 4等=" + count4 + ", 5等=" + count5 + ", 6等=" + count6);
      long totalWin = 0L;
      List<ItemStack> toRemove = new ArrayList<>();
      ItemStack[] var36;
      int var35 = (var36 = player.getInventory().getContents()).length;

      for (int var34 = 0; var34 < var35; var34++) {
         ItemStack item = var36[var34];
         if (item != null && item.getType() == Material.PAPER) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.hasLore() && ChatColor.stripColor(meta.getDisplayName()).contains("くろねこ印のたからくじ")) {
               List<String> lore = meta.getLore();
               if (lore.size() >= 6
                  && ChatColor.stripColor(lore.get(0)).contains(ym)
                  && ChatColor.stripColor(lore.get(2)).equals(player.getName())
                  && ChatColor.stripColor(lore.get(5)).matches("\\d{6}")) {
                  int from = Integer.parseInt(ChatColor.stripColor(lore.get(5)));
                  int to = from;
                  if (lore.size() >= 8 && ChatColor.stripColor(lore.get(6)).equals("～") && ChatColor.stripColor(lore.get(7)).matches("\\d{6}")) {
                     to = Integer.parseInt(ChatColor.stripColor(lore.get(7)));
                  }

                  IgaDebugLogger.log(this.getClass(), "該当くじ番号範囲: " + from + " ～ " + to);
                  long playerTotalWin = 0L;
                  long reward1base = totalSales * 30L / 100L;
                  long rewardSurplus = 0L;
                  if (count2 == 0) {
                     rewardSurplus += reward2;
                  }

                  if (count3 == 0) {
                     rewardSurplus += reward3;
                  }

                  if (count4 == 0) {
                     rewardSurplus += reward4;
                  }

                  if (count5 == 0) {
                     rewardSurplus += reward5;
                  }

                  if (count6 == 0) {
                     rewardSurplus += reward6;
                  }

                  reward1 = reward1base + rewardSurplus;
                  long totalReward1 = 0L;
                  long totalReward2 = 0L;
                  long totalReward3 = 0L;
                  long totalReward4 = 0L;
                  long totalReward5 = 0L;
                  long totalReward6 = 0L;
                  long totalFrontRear = 0L;
                  long surplus2 = count2 > 0 ? reward2 % count2 : reward2;
                  long surplus3 = count3 > 0 ? reward3 % count3 : reward3;
                  long surplus4 = count4 > 0 ? reward4 % count4 : reward4;
                  long surplus5 = count5 > 0 ? reward5 % count5 : reward5;
                  long surplus6 = count6 > 0 ? reward6 % count6 : reward6;
                  long totalSurplus = surplus2 + surplus3 + surplus4 + surplus5 + surplus6;
                  int match1Count = 0;
                  int match6Count = 0;
                  int countFrontRearWinners = 0;

                  for (int num = from; num <= to; num++) {
                     int match = this.checkWinLevel(num, winNum);
                     if (match > 0) {
                        IgaDebugLogger.log(this.getClass(), "番号 " + num + " が等級 " + match + " に当選");
                     }

                     switch (match) {
                        case 1:
                           match1Count++;
                           totalWin += reward1;
                           break;
                        case 2:
                           if (count2 > 0) {
                              long r = reward2 / count2;
                              totalWin += r;
                           }
                           break;
                        case 3:
                           if (count3 > 0) {
                              long r = reward3 / count3;
                              totalWin += r;
                           }
                           break;
                        case 4:
                           if (count4 > 0) {
                              long r = reward4 / count4;
                              totalWin += r;
                           }
                           break;
                        case 5:
                           if (count5 > 0) {
                              long r = reward5 / count5;
                              totalWin += r;
                           }
                           break;
                        case 6:
                           if (count6 > 0) {
                              long r = reward6 / count6;
                              totalWin += r;
                              match6Count++;
                           }
                        case 7:
                        case 8:
                        case 9:
                        case 10:
                        default:
                           break;
                        case 11:
                           countFrontRearWinners++;
                           totalWin += rewardFrontRear;
                     }
                  }

                  playerTotalWin += totalWin;
                  IgaDebugLogger.log(this.getClass(), "等級ごとの配分と端数");
                  IgaDebugLogger.log(this.getClass(), "等級2: reward=" + reward2 + ", count=" + count2 + ", 余り=" + surplus2);
                  IgaDebugLogger.log(this.getClass(), "等級3: reward=" + reward3 + ", count=" + count3 + ", 余り=" + surplus3);
                  IgaDebugLogger.log(this.getClass(), "等級4: reward=" + reward4 + ", count=" + count4 + ", 余り=" + surplus4);
                  IgaDebugLogger.log(this.getClass(), "等級5: reward=" + reward5 + ", count=" + count5 + ", 余り=" + surplus5);
                  IgaDebugLogger.log(this.getClass(), "等級6: reward=" + reward6 + ", count=" + count6 + ", 余り=" + surplus6);
                  IgaDebugLogger.log(this.getClass(), "端数合計: " + totalSurplus);
                  long unusedReward1 = match1Count == 0 ? reward1base : 0L;
                  long unusedFrontRear = countFrontRearWinners == 0 ? rewardFrontRear : 0L;
                  long unusedTotal = unusedReward1 + unusedFrontRear + rewardSurplus;
                  IgaDebugLogger.log(this.getClass(), "=== 使用されなかった配分額の詳細 ===");
                  IgaDebugLogger.log(this.getClass(), "1等（未配当）: " + unusedReward1);
                  IgaDebugLogger.log(this.getClass(), "前後賞（未配当）: " + unusedFrontRear);
                  IgaDebugLogger.log(this.getClass(), "等級端数（totalSurplus）: " + totalSurplus);
                  IgaDebugLogger.log(this.getClass(), "未配当合計（合算）: " + unusedTotal);
                  IgaDebugLogger.log(this.getClass(), "等級1 合計支払額: " + totalReward1);
                  IgaDebugLogger.log(this.getClass(), "前後賞 合計支払額: " + totalFrontRear);
                  IgaDebugLogger.log(this.getClass(), "等級2 合計支払額: " + totalReward2);
                  IgaDebugLogger.log(this.getClass(), "等級3 合計支払額: " + totalReward3);
                  IgaDebugLogger.log(this.getClass(), "等級4 合計支払額: " + totalReward4);
                  IgaDebugLogger.log(this.getClass(), "等級5 合計支払額: " + totalReward5);
                  IgaDebugLogger.log(this.getClass(), "等級6 合計支払額: " + totalReward6);
                  IgaDebugLogger.log(this.getClass(), "総売上: " + totalSales);
                  IgaDebugLogger.log(this.getClass(), "プレイヤー賞金合計（この束）: " + totalWin);
                  IgaDebugLogger.log(this.getClass(), "未配分金額（この束）: " + (totalSales - playerTotalWin));
                  IgaDebugLogger.log(this.getClass(), "\ud83c\udfaf プレイヤー最終賞金合計: " + playerTotalWin);
                  toRemove.add(item);
               }
            }
         }
      }

      for (ItemStack item : toRemove) {
         player.getInventory().remove(item);
      }

      IgaDebugLogger.log(this.getClass(), "アイテム回収完了: " + toRemove.size() + "個");
      if (totalWin > 0L) {
         String cmd = this.command.get(data).replace("%player%", player.getName()).replace("%amount%", String.valueOf(totalWin));
         IgaDebugLogger.log(this.getClass(), "当選金支払いコマンド実行: " + cmd);
         Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
      } else {
         IgaDebugLogger.log(this.getClass(), "当選なし: コマンド実行なし");
      }

      return SkillResult.SUCCESS;
   }

   private int checkWinLevel(int num, int win) {
      if (num == win) {
         return 1;
      }

      if (num != win - 1 && num != win + 1) {
         String numStr = String.format("%06d", num);
         String winStr = String.format("%06d", win);
         if (numStr.substring(1).equals(winStr.substring(1))) {
            return 2;
         } else if (numStr.substring(2).equals(winStr.substring(2))) {
            return 3;
         } else if (numStr.substring(3).equals(winStr.substring(3))) {
            return 4;
         } else if (numStr.substring(4).equals(winStr.substring(4))) {
            return 5;
         } else {
            return numStr.substring(5).equals(winStr.substring(5)) ? 6 : 0;
         }
      } else {
         return 11;
      }
   }

   private int countMatches(Set<Integer> pool, int win, int digits) {
      String winStr = String.format("%06d", win);
      Set<Integer> filteredPool = new HashSet<>(pool);
      if (digits < 6) {
         for (int higherDigits = digits + 1; higherDigits <= 6; higherDigits++) {
            String prefix = winStr.substring(6 - higherDigits);
            filteredPool.removeIf(nx -> String.format("%06d", nx).endsWith(prefix));
         }
      }

      String suffix = winStr.substring(6 - digits);
      int count = 0;

      for (int n : filteredPool) {
         String s = String.format("%06d", n).substring(6 - digits);
         if (s.equals(suffix)) {
            count++;
         }
      }

      return count;
   }
}
