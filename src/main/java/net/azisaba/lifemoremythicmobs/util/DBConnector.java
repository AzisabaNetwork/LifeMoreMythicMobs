package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.utils.storage.sql.hikari.HikariConfig;
import io.lumine.mythic.utils.storage.sql.hikari.HikariDataSource;
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

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static void runPrepareStatement(@NotNull String sql, @NotNull DBConnector.SqlConsumer<PreparedStatement> action) throws SQLException {
      Throwable var2 = null;
      Object var3 = null;

      try {
         Connection c = getConnection();

         try {
            PreparedStatement ps = c.prepareStatement(sql);

            try {
               action.accept(ps);
            } finally {
               if (ps != null) {
                  ps.close();
               }
            }
         } catch (Throwable var16) {
            if (var2 == null) {
               var2 = var16;
            } else if (var2 != var16) {
               var2.addSuppressed(var16);
            }

            if (c != null) {
               c.close();
            }

            throw var2;
         }

         if (c != null) {
            c.close();
         }
      } catch (Throwable var17) {
         if (var2 == null) {
            var2 = var17;
         } else if (var2 != var17) {
            var2.addSuppressed(var17);
         }

         throw var2;
      }
   }

   // $VF: Could not inline inconsistent finally blocks
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   public static <R> R getPrepareStatement(@NotNull String sql, @NotNull DBConnector.SqlFunction<PreparedStatement, R> action) throws SQLException {
      Throwable var2 = null;
      Object var3 = null;

      try {
         Connection c = getConnection();

         Object var10000;
         try {
            PreparedStatement ps = c.prepareStatement(sql);

            try {
               var10000 = action.apply(ps);
            } finally {
               if (ps != null) {
                  ps.close();
               }
            }
         } catch (Throwable var16) {
            if (var2 == null) {
               var2 = var16;
            } else if (var2 != var16) {
               var2.addSuppressed(var16);
            }

            if (c != null) {
               c.close();
            }

            throw var2;
         }

         if (c != null) {
            c.close();
         }

         return (R)var10000;
      } catch (Throwable var17) {
         if (var2 == null) {
            var2 = var17;
         } else if (var2 != var17) {
            var2.addSuppressed(var17);
         }

         throw var2;
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
