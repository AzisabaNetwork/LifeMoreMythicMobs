package net.azisaba.lifemoremythicmobs.util;

import java.util.Objects;
import java.util.Properties;
import org.bukkit.configuration.ConfigurationSection;

public class DBConfig {
   private final String driver;
   private final String jdbcUrl;
   private final String scheme;
   private final String hostname;
   private final int port;
   private final String name;
   private final String username;
   private final String password;
   private final Properties properties;

   public DBConfig(ConfigurationSection section) {
      this.driver = section.getString("driver");
      this.jdbcUrl = section.getString("jdbcUrl");
      this.scheme = section.getString("scheme");
      this.hostname = section.getString("hostname");
      this.port = section.getInt("port", 3306);
      this.name = section.getString("name");
      this.username = section.getString("user");
      this.password = section.getString("pass");
      Properties props = new Properties();
      ConfigurationSection propSec = section.getConfigurationSection("properties");
      if (propSec != null) {
         propSec.getValues(true).forEach((k, v) -> props.setProperty(k, String.valueOf(v)));
      }

      this.properties = props;
   }

   public String getDriver() {
      return this.driver;
   }

   public String getUsername() {
      return this.username;
   }

   public String getPassword() {
      return this.password;
   }

   public Properties getProperties() {
      return this.properties;
   }

   public String getJdbcUrl() {
      if (this.jdbcUrl != null && !this.jdbcUrl.isEmpty()) {
         return this.jdbcUrl;
      }

      Objects.requireNonNull(this.scheme, "database.scheme is required when jdbcUrl is not set");
      Objects.requireNonNull(this.hostname, "database.hostname is required when jdbcUrl is not set");
      Objects.requireNonNull(this.name, "database.name is required when jdbcUrl is not set");
      return this.scheme + "://" + this.hostname + ":" + this.port + "/" + this.name;
   }
}
