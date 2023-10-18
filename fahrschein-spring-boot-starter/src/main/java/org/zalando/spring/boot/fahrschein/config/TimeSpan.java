package org.zalando.spring.boot.fahrschein.config;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.zalando.fahrschein.Preconditions.checkArgument;

@AllArgsConstructor(staticName = "of")
@Getter
@Slf4j
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

    // used by SnakeYAML
    public TimeSpan(final Integer value) { this(TimeSpan.valueOf(value)); }

    private TimeSpan(final TimeSpan span) {
        this(span.amount, span.unit);
    }

    public long to(final TimeUnit targetUnit) {
        return targetUnit.convert(amount, unit);
    }

    void applyTo(final BiConsumer<Long, TimeUnit> consumer) {
        consumer.accept(amount, unit);
    }

    @Override
    public String toString() {
        return amount + " " + toName(unit);
    }

    static TimeSpan valueOf(final int value) {
       log.warn("TimeSpan without unit found. Assuming seconds.", value);
       return new TimeSpan(value, TimeUnit.SECONDS);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSpan timeSpan = (TimeSpan) o;
        return to(TimeUnit.MILLISECONDS) == timeSpan.to(TimeUnit.MILLISECONDS);
    }

    @Override
    public int hashCode() {
        return Objects.hash(to(TimeUnit.MILLISECONDS));
    }
}
