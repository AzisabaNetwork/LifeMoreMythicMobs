package net.azisaba.lifemoremythicmobs.util;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.variables.VariableRegistry;
import io.lumine.mythic.api.skills.variables.VariableScope;

public class VariableUtil {
   public static boolean setScopedVariable(String scopedVarName, String value, SkillMetadata data, AbstractEntity target) {
      if (scopedVarName != null && !scopedVarName.isEmpty()) {
         if (scopedVarName.startsWith("<") && scopedVarName.endsWith(">")) {
            scopedVarName = scopedVarName.substring(1, scopedVarName.length() - 1);
         }

         String[] parts = scopedVarName.split("\\.", 2);
         if (parts.length != 2) {
            return false;
         }

         String scopeString = parts[0].toLowerCase();
         String variableName = parts[1];
         if (variableName.startsWith("var.")) {
            variableName = variableName.substring("var.".length());
         }

         String registry = scopeString;
         VariableScope scope;
         switch (scopeString.hashCode()) {
            case -1367559124:
               if (!registry.equals("caster")) {
                  return false;
               }

               scope = VariableScope.CASTER;
               break;
            case -1243020381:
               if (!registry.equals("global")) {
                  return false;
               }

               scope = VariableScope.GLOBAL;
               break;
            case -880905839:
               if (!registry.equals("target")) {
                  return false;
               }

               scope = VariableScope.TARGET;
               break;
            case 109496913:
               if (!registry.equals("skill")) {
                  return false;
               }

               scope = VariableScope.SKILL;
               break;
            case 113318802:
               if (registry.equals("world")) {
                  scope = VariableScope.WORLD;
                  break;
               }

               return false;
            default:
               return false;
         }

         VariableRegistry registry = MythicBukkit.inst().getVariableManager().getRegistry(scope, data, target);
         registry.putString(variableName, value);
         return true;
      } else {
         return false;
      }
   }
}
