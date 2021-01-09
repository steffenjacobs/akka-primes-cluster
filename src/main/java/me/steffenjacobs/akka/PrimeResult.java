package me.steffenjacobs.akka;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class PrimeResult {
    private final List<Long> results;

    public PrimeResult(List<Long> results) {
        this.results = results;
    }

    public List<Long> getResults() {
        return results;
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * (results.size() + 1));
        buffer.putLong(results.size());
        results.forEach(buffer::putLong);
        return buffer.array();
    }

    public static PrimeResult parseFrom(byte[] bytes) {
        ByteBuffer buffer = (ByteBuffer) ((Buffer) ByteBuffer.allocate(bytes.length).put(bytes)).flip();
        long size = buffer.getLong();
        List<Long> results = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            results.add(buffer.getLong());
        }
        return new PrimeResult(results);
    }
}
