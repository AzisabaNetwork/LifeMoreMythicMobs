package net.azisaba.lifemoremythicmobs.util;

import java.sql.SQLException;

public class TimerTableInitializer {
   private TimerTableInitializer() {
   }

   public static void createIfNotExists(String jdbcUrl) throws SQLException {
      boolean mysql = jdbcUrl != null && (jdbcUrl.startsWith("jdbc:mysql:") || jdbcUrl.startsWith("jdbc:mariadb:"));
      if (mysql) {
         createMySql();
      } else {
         createSqlite();
      }
   }

   private static void createMySql() throws SQLException {
      String sql = "CREATE TABLE IF NOT EXISTS player_time_measure ( uuid VARCHAR(36) NOT NULL, purpose VARCHAR(64) NOT NULL, mcid VARCHAR(16) NOT NULL, elapsed_sec INT NOT NULL, updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, PRIMARY KEY (uuid, purpose))";
      DBConnector.runPrepareStatement(sql, ps -> ps.execute());
   }

   private static void createSqlite() throws SQLException {
      String sql = "CREATE TABLE IF NOT EXISTS player_time_measure ( uuid TEXT NOT NULL, purpose TEXT NOT NULL, mcid TEXT NOT NULL, elapsed_sec INTEGER NOT NULL, updated_at TEXT NOT NULL, PRIMARY KEY (uuid, purpose))";
      DBConnector.runPrepareStatement(sql, ps -> ps.execute());
   }

   public static void dropAndCreate(String jdbcUrl) throws SQLException {
      DBConnector.runPrepareStatement("DROP TABLE IF EXISTS player_time_measure", ps -> ps.execute());
      createIfNotExists(jdbcUrl);
   }
}
