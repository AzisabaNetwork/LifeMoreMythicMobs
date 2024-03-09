package net.azisaba.lifemoremythicmobs.condition;

import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.SkillCondition;
import io.lumine.xikage.mythicmobs.skills.conditions.IEntityCondition;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DayOfWeekCondition extends SkillCondition implements IEntityCondition {
    private final DayOfWeek dayOfWeek;

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
        String day = config.getString(new String[] {"day", "d", "曜日"}, "monday").toLowerCase(Locale.ENGLISH);
        this.dayOfWeek = DAY_MAPPING.get(day);
    }

    @Override
    public boolean check(AbstractEntity abstractEntity) {
        LocalDate now = LocalDate.now();
        return dayOfWeek == now.getDayOfWeek();
    }
}
