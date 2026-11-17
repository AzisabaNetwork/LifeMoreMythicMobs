package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.conditions.IEntityCondition;
import io.lumine.mythic.core.skills.SkillCondition;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DayOfWeekCondition extends SkillCondition implements IEntityCondition {
    private final Set<DayOfWeek> daysOfWeek = EnumSet.noneOf(DayOfWeek.class);

    private static final Map<String, DayOfWeek> DAY_MAPPING = new HashMap<String, DayOfWeek>() {{
        put("sunday", DayOfWeek.SUNDAY);
        put("monday", DayOfWeek.MONDAY);
        put("tuesday", DayOfWeek.TUESDAY);
        put("wednesday", DayOfWeek.WEDNESDAY);
        put("thursday", DayOfWeek.THURSDAY);
        put("friday", DayOfWeek.FRIDAY);
        put("saturday", DayOfWeek.SATURDAY);
    }};

    public DayOfWeekCondition(MythicLineConfig config) {
        super(config.getLine());
        String rawDays = config.getString(new String[] {"day", "d", "曜日"}, "monday");
        for (String token : ConditionMatchUtil.parseCommaSeparated(rawDays)) {
            DayOfWeek mapped = DAY_MAPPING.get(token.toLowerCase(Locale.ENGLISH));
            if (mapped != null) {
                this.daysOfWeek.add(mapped);
            }
        }
    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {
        LocalDate now = LocalDate.now();
        return this.daysOfWeek.contains(now.getDayOfWeek());
    }
}
