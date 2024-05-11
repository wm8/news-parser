package ru.dz.Tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public abstract class Task {
    public enum Type {
        DOWNLOAD_PAGE(0),
        PARSE_MAIN(1),
        PARSE_SECTION(2),
        PARSE_NEWS(3),
        UNKNOWN(-1);

        private final Integer code;

        Type(Integer code) {
            this.code = code;
        }

        @JsonValue
        public Integer getCode() {
            return code;
        }

        @JsonCreator
        public static Type fromValue(int value) {
            for (Type myEnum : Type.values()) {
                if (myEnum.code == value) {
                    return myEnum;
                }
            }
            throw new IllegalArgumentException("Invalid value: " + value);
        }
    }

    protected Type type;

    public Type getType() { return type; }

    public abstract void run();

    @Override
    public String toString() {
        return getType().toString();
    }
}

