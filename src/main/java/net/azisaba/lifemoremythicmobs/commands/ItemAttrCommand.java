package net.azisaba.lifemoremythicmobs.commands;

import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class ItemAttrCommand implements CommandExecutor, TabCompleter {
   public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
      if (!(s instanceof Player)) {
         s.sendMessage("Player only");
         return true;
      } else {
         Player p = (Player)s;
         JavaPlugin.getPlugin(LifeMoreMythicMobs.class).getGui().openMainMenu(p);
         return true;
      }
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      return Collections.emptyList();
   }
}
