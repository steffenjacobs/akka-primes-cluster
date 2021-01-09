package me.steffenjacobs.akka;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class StringMessage {
    private final String message;

    public StringMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringMessage that = (StringMessage) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message);
    }

    public byte[] toByteArray() {
        return this.message.getBytes(StandardCharsets.UTF_8);
    }

    public static StringMessage parseFrom(byte[] bytes) {
        return new StringMessage(new String(bytes, StandardCharsets.UTF_8));
    }
}
