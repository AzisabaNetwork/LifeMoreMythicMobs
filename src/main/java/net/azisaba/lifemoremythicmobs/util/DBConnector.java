package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.bukkit.utils.storage.sql.hikari.HikariConfig;
import io.lumine.mythic.bukkit.utils.storage.sql.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import javax.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

public class DBConnector {
   @Nullable
   private static HikariDataSource dataSource;

   public static void init(@NotNull DBConfig config) {
      HikariConfig hc = new HikariConfig();
      if (config.getDriver() != null && !config.getDriver().isEmpty()) {
         hc.setDriverClassName(config.getDriver());
      }

      hc.setJdbcUrl(config.getJdbcUrl());
      if (config.getUsername() != null && !config.getUsername().isEmpty()) {
         hc.setUsername(config.getUsername());
      }

      if (config.getPassword() != null && !config.getPassword().isEmpty()) {
         hc.setPassword(config.getPassword());
      }

      hc.setDataSourceProperties(config.getProperties());
      dataSource = new HikariDataSource(hc);
   }

   public static void close() {
      if (dataSource != null) {
         dataSource.close();
      }

      dataSource = null;
   }

   private static HikariDataSource ds() {
      return Objects.requireNonNull(dataSource, "DBConnector.init() was not called");
   }

   public static Connection getConnection() throws SQLException {
      return ds().getConnection();
   }

   // Reconstructed as the equivalent try-with-resources implementation.
   public static void runPrepareStatement(@NotNull String sql, @NotNull DBConnector.SqlConsumer<PreparedStatement> action) throws SQLException {
      try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
         action.accept(ps);
      }
   }

   // Reconstructed as the equivalent try-with-resources implementation.
   public static <R> R getPrepareStatement(@NotNull String sql, @NotNull DBConnector.SqlFunction<PreparedStatement, R> action) throws SQLException {
      try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
         return action.apply(ps);
      }
   }

   @FunctionalInterface
   public interface SqlConsumer<T> {
      void accept(T var1) throws SQLException;
   }

   @FunctionalInterface
   public interface SqlFunction<T, R> {
      R apply(T var1) throws SQLException;
   }
}

