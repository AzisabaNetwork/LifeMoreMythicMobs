package lifemoremythicmobs.org.example.lifemoremythicmobs.Condition;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.adapters.AbstractPlayer;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;

public class ItemLoreCondition extends SkillCondition implements IEntityCondition {

    protected final boolean hotBar;
    protected final boolean allLore; // 全てのLoreLineを参照するか (falseだとloreLineで指定したlineを参照)
    protected final boolean sentenceMatch; // Loreの文章が完全一致するか(falseだと一部分に指定されたものが入っていたらtrue)
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
