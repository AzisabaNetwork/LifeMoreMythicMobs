package net.azisaba.lifemoremythicmobs.listener;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class DailyScoreResetter {
   private static final String SERVER_NAME = "lifeevent";
   private static final String GLOBAL_FAKE_PLAYER = "__GLOBAL__";
   private static final String OBJECTIVE_EVENT_TODAY = "eventtoday";
   private static final String OBJECTIVE_WAVE_SANKA = "waveevent_sanka";
   private static final String OBJECTIVE_WAVE_KAKIN = "waveevent_kakin";

   public static void run() {
      LocalTime now = LocalTime.now();
      if (now.isBefore(LocalTime.of(4, 0))) {
         IgaDebugLogger.log("[DailyScoreResetter]", "現在時刻がAM0:00～4:00のため、処理をスキップします。");
      } else {
         String todayStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
         int todayInt = Integer.parseInt(todayStr);
         Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
         Objective eventTodayObj = board.getObjective("eventtoday");
         if (eventTodayObj == null) {
            IgaDebugLogger.log("[DailyScoreResetter]", "Objective 'eventtoday' が存在しません。スキップします。");
         } else {
            Score globalScore = eventTodayObj.getScore("__GLOBAL__");

            int storedDate;
            try {
               storedDate = globalScore.getScore();
            } catch (IllegalStateException e) {
               IgaDebugLogger.log("[DailyScoreResetter]", "スコア 'eventtoday' にスコアが設定されていません。");
               storedDate = -1;
            }

            if (storedDate != todayInt) {
               IgaDebugLogger.log("[DailyScoreResetter]", "日付が更新されたため、スコアをリセットします。");
               removeObjectiveIfExists(board, "waveevent_sanka");
               removeObjectiveIfExists(board, "waveevent_kakin");
               globalScore.setScore(todayInt);
               IgaDebugLogger.log("[DailyScoreResetter]", "'__GLOBAL__' のスコア 'eventtoday' を " + todayInt + " に更新しました。");
            } else {
               IgaDebugLogger.log("[DailyScoreResetter]", "'eventtoday' は本日と同じ (" + todayInt + ") のため、リセットしません。");
            }
         }
      }
   }

   private static void removeObjectiveIfExists(Scoreboard board, String objectiveName) {
      Objective obj = board.getObjective(objectiveName);
      if (obj != null) {
         obj.unregister();
         IgaDebugLogger.log("[DailyScoreResetter]", "Objective '" + objectiveName + "' を削除しました。");
      } else {
         IgaDebugLogger.log("[DailyScoreResetter]", "Objective '" + objectiveName + "' は存在しません。スキップします。");
      }
   }
}
