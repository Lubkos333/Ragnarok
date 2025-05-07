package cz.ragnarok.ragnarok.rest.enums;

public enum FlowType {
    KEYWORDS("keywords"),
    CLASSIC("classic"),
    PARAPHRASE("paraphrase");

    private final String value;

    FlowType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static FlowType fromValue(String value) {
        for (FlowType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown FlowType: " + value);
    }
}
