package ru.zundonza.network;

import net.minecraft.network.packet.Packet;

import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PacketLogger {
    public enum Direction { INCOMING, OUTGOING }

    public static class PacketEntry {
        public final Packet<?> packet;
        public final Direction direction;
        public final long timestamp;

        public PacketEntry(Packet<?> packet, Direction direction) {
            this.packet = packet;
            this.direction = direction;
            this.timestamp = System.currentTimeMillis();
        }
    }

    private static final int MAX_ENTRIES = 500;
    private static final ArrayDeque<PacketEntry> log = new ArrayDeque<>();
    private static final List<PacketEntry> snapshot = new CopyOnWriteArrayList<>();
    private static boolean paused = false;

    public static void log(Packet<?> packet, Direction direction) {
        if (paused) return;
        synchronized (log) {
            if (log.size() >= MAX_ENTRIES) log.pollFirst();
            PacketEntry entry = new PacketEntry(packet, direction);
            log.addLast(entry);
            snapshot.add(entry);
        }
    }

    public static List<PacketEntry> getSnapshot() {
        return snapshot;
    }

    public static void clear() {
        synchronized (log) {
            log.clear();
            snapshot.clear();
        }
    }

    public static void togglePause() {
        paused = !paused;
    }

    public static void setPaused(boolean paused) {
        PacketLogger.paused = paused;
    }

    public static boolean isPaused() {
        return paused;
    }
}