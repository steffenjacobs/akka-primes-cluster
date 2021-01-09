package me.steffenjacobs.akka;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public class SegmentMessage {
    private final long start;
    private final long end;

    public SegmentMessage(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getEnd() {
        return end;
    }

    public long getStart() {
        return start;
    }

    public byte[] toByteArray() {
        return ByteBuffer.allocate(Long.BYTES * 2).putLong(this.start).putLong(this.end).array();
    }

    public static SegmentMessage parseFrom(byte[] bytes) {
        ByteBuffer buffer = (ByteBuffer) ((Buffer)ByteBuffer.allocate(bytes.length).put(bytes)).flip();
        return new SegmentMessage(buffer.getLong(), buffer.getLong());
    }
}
