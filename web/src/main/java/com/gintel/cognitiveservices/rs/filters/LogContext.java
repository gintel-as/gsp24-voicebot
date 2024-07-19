package com.gintel.cognitiveservices.rs.filters;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class LogContext {
    private static class NameValuePair {
        private final String name;
        private final Object value;

        NameValuePair(final String name, final Object value) {
            this.name = name != null ? name : "null";
            this.value = value;
        }

        @Override
        public String toString() {
            final String valueStr = value != null ? value.toString() : "null";
            final String quoteName = name.contains(" ") ? "\"" : "";
            final String quoteValue = valueStr.contains(" ") ? "\"" : "";
            return String.join("", quoteName, name, quoteName, "=", quoteValue, valueStr, quoteValue);
        }
    }

    private final List<LogContext.NameValuePair> entries = new LinkedList<>();

    public LogContext add(final String name, final Object value) {
        entries.add(new LogContext.NameValuePair(name, value));
        return this;
    }

    public LogContext add(final String name) {
        entries.add(new LogContext.NameValuePair(name, ""));
        return this;
    }

    public LogContext addNonNull(final String name, final Object value) {
        if (value != null) {
            entries.add(new LogContext.NameValuePair(name, value));
        }
        return this;
    }

    @Override
    public String toString() {
        return entries.stream().map(Object::toString).collect(Collectors.joining("\n\t"));
    }
}
