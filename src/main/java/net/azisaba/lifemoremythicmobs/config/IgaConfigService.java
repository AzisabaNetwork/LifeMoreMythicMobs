package net.azisaba.lifemoremythicmobs.config;

import net.azisaba.lifemoremythicmobs.util.IgaDebugLogger;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class IgaConfigService {
   private final JavaPlugin plugin;
   private Set<String> configuredWorldsLower = Collections.emptySet();
   private Set<String> configuredRegionsLower = Collections.emptySet();
   private boolean emptyIsMatchAll = false;

   public IgaConfigService(JavaPlugin plugin) {
      this.plugin = plugin;
   }

   public void reload() {
      this.plugin.reloadConfig();
      FileConfiguration config = this.plugin.getConfig();
      ConfigurationSection base = config.getConfigurationSection("igaConditions.worldNotInConfig");
      List<String> worlds = base != null ? base.getStringList("worlds") : Collections.emptyList();
      List<String> regions = base != null ? base.getStringList("regions") : Collections.emptyList();
      this.configuredWorldsLower = normalize(worlds);
      this.configuredRegionsLower = normalize(regions);
      IgaDebugLogger.log(this.getClass(), "loaded: worlds=" + this.configuredWorldsLower + ", regions=" + this.configuredRegionsLower);
   }

   private static Set<String> normalize(List<String> list) {
      return list == null
         ? Collections.emptySet()
         : list.stream()
            .filter(Objects::nonNull)
            .map(s -> s.trim().toLowerCase(Locale.ROOT))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toCollection(LinkedHashSet::new));
   }

   public Set<String> getConfiguredWorldsLower() {
      return this.configuredWorldsLower;
   }

   public Set<String> getConfiguredRegionsLower() {
      return this.configuredRegionsLower;
   }

   public boolean isEmptyIsMatchAll() {
      return this.emptyIsMatchAll;
   }
}
