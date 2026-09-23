package com.campustrade.common;
import java.util.concurrent.atomic.AtomicLong;
public class IdGenerator {
    private static final AtomicLong id = new AtomicLong(System.currentTimeMillis());
    public static long nextId() { return id.incrementAndGet(); }
}
