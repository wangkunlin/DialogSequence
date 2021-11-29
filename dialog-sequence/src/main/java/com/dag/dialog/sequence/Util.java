package com.dag.dialog.sequence;

import java.util.Objects;

/**
 * On 2021-11-27
 */
class Util {

    static String join(CharSequence delimiter,
                                   Iterable<? extends CharSequence> elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        StringJoiner joiner = new StringJoiner(delimiter);
        for (CharSequence cs : elements) {
            joiner.add(cs);
        }
        return joiner.toString();
    }

    static class StringJoiner {
        private final String prefix;
        private final String delimiter;
        private final String suffix;

        private StringBuilder value;

        private String emptyValue;

        public StringJoiner(CharSequence delimiter) {
            this(delimiter, "", "");
        }

        public StringJoiner(CharSequence delimiter,
                            CharSequence prefix,
                            CharSequence suffix) {
            Objects.requireNonNull(prefix, "The prefix must not be null");
            Objects.requireNonNull(delimiter, "The delimiter must not be null");
            Objects.requireNonNull(suffix, "The suffix must not be null");
            // make defensive copies of arguments
            this.prefix = prefix.toString();
            this.delimiter = delimiter.toString();
            this.suffix = suffix.toString();
            this.emptyValue = this.prefix + this.suffix;
        }

        public StringJoiner setEmptyValue(CharSequence emptyValue) {
            this.emptyValue = Objects.requireNonNull(emptyValue,
                    "The empty value must not be null").toString();
            return this;
        }

        @Override
        public String toString() {
            if (value == null) {
                return emptyValue;
            } else {
                if (suffix.equals("")) {
                    return value.toString();
                } else {
                    int initialLength = value.length();
                    String result = value.append(suffix).toString();
                    // reset value to pre-append initialLength
                    value.setLength(initialLength);
                    return result;
                }
            }
        }

        public StringJoiner add(CharSequence newElement) {
            prepareBuilder().append(newElement);
            return this;
        }

        public StringJoiner merge(StringJoiner other) {
            Objects.requireNonNull(other);
            if (other.value != null) {
                final int length = other.value.length();
                // lock the length so that we can seize the data to be appended
                // before initiate copying to avoid interference, especially when
                // merge 'this'
                StringBuilder builder = prepareBuilder();
                builder.append(other.value, other.prefix.length(), length);
            }
            return this;
        }

        private StringBuilder prepareBuilder() {
            if (value != null) {
                value.append(delimiter);
            } else {
                value = new StringBuilder().append(prefix);
            }
            return value;
        }

        public int length() {
            // Remember that we never actually append the suffix unless we return
            // the full (present) value or some sub-string or length of it, so that
            // we can add on more if we need to.
            return (value != null ? value.length() + suffix.length() :
                    emptyValue.length());
        }
    }
}
