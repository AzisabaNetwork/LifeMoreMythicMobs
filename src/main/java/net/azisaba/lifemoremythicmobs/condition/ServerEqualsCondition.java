package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;

import java.util.List;

public class ServerEqualsCondition extends SkillCondition implements ISkillMetaCondition {
    private final boolean invert;
    private final List<String> servers;

    public ServerEqualsCondition(MythicLineConfig config) {
        super(config.getLine());

        this.invert = config.getBoolean(new String[] {"invert", "i", "逆転"}, false);
        this.servers = ConditionMatchUtil.parseCommaSeparated(config.getString(new String[] {"server", "s", "サーバー"}));
    }

    @Override
    public boolean check(SkillMetadata skillMetadata) {
        LifeMoreMythicMobs plugin = LifeMoreMythicMobs.getPlugin(LifeMoreMythicMobs.class);
        String serverName = plugin.server;
        return invert != ConditionMatchUtil.anyEquals(serverName, servers, false);
    }
}
