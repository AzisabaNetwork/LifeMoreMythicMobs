package net.azisaba.lifemoremythicmobs.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Logger;

public class TimerRepository {
   private final boolean mysql;
   private final Logger logger;

   public TimerRepository(String jdbcUrl, Logger logger) {
      this.mysql = jdbcUrl != null && (jdbcUrl.startsWith("jdbc:mysql:") || jdbcUrl.startsWith("jdbc:mariadb:"));
      this.logger = logger;
   }

   public void upsert(UUID uuid, String purpose, String mcid, int elapsedSec) throws Exception {
      if (this.mysql) {
         this.upsertMySql(uuid, purpose, mcid, elapsedSec);
      } else {
         this.upsertSqlite(uuid, purpose, mcid, elapsedSec);
      }
   }

   private void upsertMySql(UUID uuid, String purpose, String mcid, int elapsedSec) throws Exception {
      String sql = "INSERT INTO player_time_measure(uuid, purpose, mcid, elapsed_sec) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE mcid=VALUES(mcid), elapsed_sec=VALUES(elapsed_sec)";
      DBConnector.runPrepareStatement(sql, ps -> {
         ps.setString(1, uuid.toString());
         ps.setString(2, purpose);
         ps.setString(3, mcid);
         ps.setInt(4, elapsedSec);
         ps.executeUpdate();
      });
   }

   private void upsertSqlite(UUID uuid, String purpose, String mcid, int elapsedSec) throws Exception {
      String sql = "INSERT INTO player_time_measure(uuid, purpose, mcid, elapsed_sec, updated_at) VALUES(?, ?, ?, ?, ?) ON CONFLICT(uuid, purpose) DO UPDATE SET mcid=excluded.mcid, elapsed_sec=excluded.elapsed_sec, updated_at=excluded.updated_at";
      DBConnector.runPrepareStatement(sql, ps -> {
         ps.setString(1, uuid.toString());
         ps.setString(2, purpose);
         ps.setString(3, mcid);
         ps.setInt(4, elapsedSec);
         ps.setString(5, Instant.now().toString());
         ps.executeUpdate();
      });
   }

   public int getElapsedSec(UUID uuid, String purpose) throws SQLException {
      String sql = "SELECT elapsed_sec FROM player_time_measure WHERE uuid = ? AND purpose = ?";
      if (this.logger != null) {
         this.logger.info("[timer] SELECT elapsed_sec (uuid=" + uuid + ", purpose=" + purpose + ")");
      }

      return DBConnector.getPrepareStatement(sql, ps -> {
         ps.setString(1, uuid.toString());
         ps.setString(2, purpose);
         Throwable var3x = null;
         Object var4 = null;

         try {
            ResultSet rs = ps.executeQuery();

            Integer var10000;
            try {
               if (!rs.next()) {
                  return 0;
               }

               var10000 = rs.getInt("elapsed_sec");
            } finally {
               if (rs != null) {
                  rs.close();
               }
            }

            return var10000;
         } catch (Throwable var11) {
            if (var3x == null) {
               var3x = var11;
            } else if (var3x != var11) {
               var3x.addSuppressed(var11);
            }

            throw var3x;
         }
      });
   }
}
