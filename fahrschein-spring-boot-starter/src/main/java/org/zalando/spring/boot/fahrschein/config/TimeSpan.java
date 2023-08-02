package org.zalando.spring.boot.fahrschein.config;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public final class TimeSpan {

    private static final Pattern PATTERN = Pattern.compile("(\\d+) (\\w+)");

    private static final Map<String, TimeUnit> UNITS = Arrays.stream(TimeUnit.values())
            .collect(toMap(TimeSpan::toName, identity()));

    private final long amount;
    private final TimeUnit unit;

    // used by SnakeYAML
    public TimeSpan(final String value) {
        this(TimeSpan.valueOf(value));
    }

    private TimeSpan(final TimeSpan span) {
        this(span.amount, span.unit);
    }

    private TimeSpan(long amount, TimeUnit unit) {
        this.amount = amount;
        this.unit = unit;
    }

    public static TimeSpan of(long amount, TimeUnit unit) {
        return new TimeSpan(amount, unit);
    }

    long to(final TimeUnit targetUnit) {
        return targetUnit.convert(amount, unit);
    }

    void applyTo(final BiConsumer<Long, TimeUnit> consumer) {
        consumer.accept(amount, unit);
    }

    @Override
    public String toString() {
        return amount + " " + toName(unit);
    }

    static TimeSpan valueOf(final String value) {
        if (value.isEmpty()) {
            return new TimeSpan(0, TimeUnit.NANOSECONDS);
        }

        final Matcher matcher = PATTERN.matcher(value);
        checkArgument(matcher.matches(), "'%s' is not a valid time span", value);

        final long amount = Long.parseLong(matcher.group(1));
        final TimeUnit unit = fromName(matcher.group(2));

        return new TimeSpan(amount, unit);
    }

    private static TimeUnit fromName(final String name) {
        return parse(name.toLowerCase(Locale.ROOT));
    }

    private static TimeUnit parse(final String name) {
        final TimeUnit unit = UNITS.get(name.endsWith("s") ? name : name + "s");
        checkArgument(unit != null, "Unknown time unit: [%s]", name);
        return unit;
    }

    private static String toName(final TimeUnit unit) {
        return unit.name().toLowerCase(Locale.ROOT);
    }

    public long getAmount() {
        return this.amount;
    }

    public TimeUnit getUnit() {
        return this.unit;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof TimeSpan)) return false;
        final TimeSpan other = (TimeSpan) o;
        if (this.getAmount() != other.getAmount()) return false;
        final Object this$unit = this.getUnit();
        final Object other$unit = other.getUnit();
        if (this$unit == null ? other$unit != null : !this$unit.equals(other$unit)) return false;
        return true;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $amount = this.getAmount();
        result = result * PRIME + (int) ($amount >>> 32 ^ $amount);
        final Object $unit = this.getUnit();
        result = result * PRIME + ($unit == null ? 43 : $unit.hashCode());
        return result;
    }
}
