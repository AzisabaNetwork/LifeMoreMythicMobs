package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition;
import io.lumine.mythic.core.skills.SkillCondition;
import net.azisaba.lifemoremythicmobs.LifeMoreMythicMobs;

public class ServerEqualsCondition extends SkillCondition implements ISkillMetaCondition {
    private final boolean invert;
    private final String server;

    public ServerEqualsCondition(MythicLineConfig config) {
        super(config.getLine());

        this.invert = config.getBoolean(new String[] {"invert", "i", "逆転"}, false);
        this.server = config.getString(new String[] {"server", "s", "サーバー"});
    }

    @Override
    public boolean check(SkillMetadata skillMetadata) {
        LifeMoreMythicMobs plugin = LifeMoreMythicMobs.getPlugin(LifeMoreMythicMobs.class);
        String serverName = plugin.server;
        return invert != serverName.equals(server);
    }
}
