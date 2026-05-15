package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.adapters.AbstractPlayer;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.core.skills.SkillCondition;

public class ItemLoreCondition extends SkillCondition implements IEntityCondition {

    protected final boolean hotBar;
    protected final boolean allLore;
    protected final boolean sentenceMatch;
    protected final int hotBarNum;
    protected final int loreLine;
    protected final String lore;

    public void checkItemLore(AbstractPlayer p) {




    }

    public ItemLoreCondition(MythicLineConfig config) {

        super(config.getLine());

        this.hotBar = config.getBoolean(new String[]{"hotbar", "hb"}, false);
        this.allLore = config.getBoolean(new String[]{"alllore", "all", "al"}, false);
        this.sentenceMatch = config.getBoolean(new String[]{"sentencematch", "sentencem", "smatch", "sm"}, false);
        if ( hotBar ) {
            this.hotBarNum = config.getInteger(new String[]{"hotbarnum", "hbn"});
        } else {
            this.hotBarNum = config.getInteger(new String[]{"hotbarnum", "hbn"}, 1);
        }
        if ( allLore ) {
            this.loreLine = config.getInteger(new String[]{"loreline", "ll"}, 1);
        } else {
            this.loreLine = config.getInteger(new String[]{"loreline", "ll"});
        }
        this.lore = config.getString(new String[]{"lore", "l"});

    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {




        return true;
    }
}
