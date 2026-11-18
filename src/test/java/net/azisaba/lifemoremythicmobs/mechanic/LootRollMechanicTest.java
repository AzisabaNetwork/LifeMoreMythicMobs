package net.azisaba.lifemoremythicmobs.mechanic;

import net.azisaba.lifemoremythicmobs.util.LootTableUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LootRollMechanicTest {
    @Test
    void parsesItemSkillAndNothingEntries() {
        List<LootTableUtil.LootEntry> entries = LootTableUtil.parseEntries(
                "StoneShard:60:2,skill:RareSkill:10,nothing:30"
        );
        assertEquals(3, entries.size());
        assertEquals(LootTableUtil.EntryKind.ITEM, entries.get(0).kind);
        assertEquals("StoneShard", entries.get(0).target);
        assertEquals(2, entries.get(0).amount);
        assertEquals(60.0, entries.get(0).baseWeight);

        assertEquals(LootTableUtil.EntryKind.SKILL, entries.get(1).kind);
        assertEquals("RareSkill", entries.get(1).target);

        assertEquals(LootTableUtil.EntryKind.NOTHING, entries.get(2).kind);
    }

    @Test
    void luckFactorBoostsRareEntryProbability() {
        List<LootTableUtil.LootEntry> entries = LootTableUtil.parseEntries("Common:90,Rare:10");
        assertEquals("Common", LootTableUtil.pickEntry(entries, 0, 0.1, 0.85).target);
        assertEquals("Rare", LootTableUtil.pickEntry(entries, 10, 0.5, 0.85).target);
    }
}
