package project.ktc.springboot_app.chat.enums;

/**
 * Enum representing different types of chat messages
 */
public enum MessageType {
    TEXT("text"),
    FILE("file"),
    VIDEO("video"),
    AUDIO("audio");

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MessageType fromValue(String value) {
        for (MessageType type : MessageType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid message type: " + value);
    }

    public boolean isMediaType() {
        return this == FILE || this == VIDEO || this == AUDIO;
    }

    public boolean isTextType() {
        return this == TEXT;
    }
}
