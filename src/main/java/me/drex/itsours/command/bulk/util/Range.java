package me.drex.itsours.command.bulk.util;

public class Range {

    private Type type;
    private int integer;

    public Range(Type type, int integer) {
        this.type = type;
        this.integer = integer;
    }

    public Range(String input) {
        if (input.startsWith(">=")) {
            this.type = Type.MIN_INCLUSIVE;
            this.integer = Integer.parseInt(input.substring(2));
        } else if (input.startsWith("<=")) {
            this.type = Type.MAX_INCLUSIVE;
            this.integer = Integer.parseInt(input.substring(2));
        } else if (input.startsWith("=")) {
            this.type = Type.EXACT;
            this.integer = Integer.parseInt(input.substring(1));
        } else if (input.startsWith("<")) {
            this.type = Type.MAX_EXCLUSIVE;
            this.integer = Integer.parseInt(input.substring(1));
        } else if (input.startsWith(">")) {
            this.type = Type.MIN_EXCLUSIVE;
            this.integer = Integer.parseInt(input.substring(1));
        } else {
            this.type = Type.EXACT;
            this.integer = Integer.parseInt(input);
        }
    }

    public boolean includes(int input) {
        switch (this.type) {
            case MIN_INCLUSIVE:
                return input >= this.integer;
            case MAX_INCLUSIVE:
                return input <= this.integer;
            case EXACT:
                return input == this.integer;
            case MAX_EXCLUSIVE:
                return input < this.integer;
            case MIN_EXCLUSIVE:
                return input > this.integer;
            default:
                return false;
        }
    }

    public boolean includes(int min, int max) {
        switch (this.type) {
            case MIN_INCLUSIVE:
            case MIN_EXCLUSIVE:
                return includes(max);
            case MAX_INCLUSIVE:
            case MAX_EXCLUSIVE:
                return includes(min);
            case EXACT:
                return includes(min) || includes(max);
            default:
                return false;
        }
    }

    public int getInteger() {
        return integer;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        EXACT(),
        MIN_EXCLUSIVE(),
        MIN_INCLUSIVE(),
        MAX_EXCLUSIVE(),
        MAX_INCLUSIVE();
    }

}
