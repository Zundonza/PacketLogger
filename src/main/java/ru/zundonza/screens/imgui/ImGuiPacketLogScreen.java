package ru.zundonza.screens.imgui;

import imgui.ImGui;
import imgui.flag.ImGuiTabBarFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import ru.zundonza.ReflectionUtils;
import ru.zundonza.network.PacketLogger;
import ru.zundonza.network.PacketLogger.PacketEntry;
import ru.zundonza.network.PacketLogger.Direction;
import ru.zundonza.network.PacketNameMapper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImGuiPacketLogScreen {
    private static final String[] tabs = { "All", "Outgoing", "Incoming" };
    private static final ImString[] filters = new ImString[tabs.length];
    private static final int[] maxDepth = {8};

    private static final AtomicBoolean isExporting = new AtomicBoolean(false);
    private static final AtomicBoolean wasPaused = new AtomicBoolean(false);

    static {
        for (int i = 0; i < filters.length; i++) {
            filters[i] = new ImString(64);
        }

        ReflectionUtils.setMaxDepth(maxDepth[0]);
    }

    public static void render() {
        if (ImGui.begin("PacketLogger")) {
            if (ImGui.button(PacketLogger.isPaused() ? "Continue" : "Pause")) {
                PacketLogger.togglePause();
            }
            ImGui.sameLine();
            if (ImGui.button("Clear")) {
                PacketLogger.clear();
            }

            ImGui.sameLine();
            ImGui.setNextItemWidth(100);
            if (ImGui.sliderInt("Max Depth", maxDepth, 1, 18)) {
                ReflectionUtils.setMaxDepth(maxDepth[0]);
            }
            ImGui.sameLine();
            ImGui.textDisabled("(?)");
            if (ImGui.isItemHovered()) {
                ImGui.beginTooltip();
                ImGui.text("Controls how deep to show nested objects");
                ImGui.endTooltip();
            }

            ImGui.separator();

            if (ImGui.beginTabBar("packet_tabs", ImGuiTabBarFlags.None)) {
                for (int i = 0; i < tabs.length; i++) {
                    if (ImGui.beginTabItem(tabs[i])) {
                        ImGui.inputText("Filter##" + i, filters[i]);

                        ImGui.sameLine();
                        boolean exporting = isExporting.get();
                        if (exporting) {
                            ImGui.beginDisabled();
                        }

                        if (ImGui.button("Export to TXT##" + i)) {
                            startExport(i, filters[i].get());
                        }

                        if (exporting) {
                            ImGui.endDisabled();
                        }

                        for (PacketEntry entry : PacketLogger.getSnapshot()) {
                            if (!matchesTab(entry, i)) continue;

                            String name = PacketNameMapper.NAME_MAP.getOrDefault(entry.packet.getClass(), entry.packet.getClass().getSimpleName());
                            if (!name.toLowerCase().contains(filters[i].get().toLowerCase())) continue;

                            if (ImGui.treeNodeEx(name + "##" + entry.timestamp, ImGuiTreeNodeFlags.FramePadding)) {
                                try {
                                    ImGui.text("Class: " + entry.packet.getClass().getName());
                                    ImGui.text("Type: " + entry.direction);
                                    ImGui.text("Time: " + entry.timestamp);
                                    ImGui.separator();
                                    ImGui.textWrapped("toString(): " + entry.packet);
                                    ImGui.separator();
                                    ReflectionUtils.renderObjectTree("Packet", entry.packet, 0);
                                } finally {
                                    ImGui.treePop();
                                }
                            }
                        }

                        ImGui.endTabItem();
                    }
                }
                ImGui.endTabBar();
            }

            ImGui.end();
        }
    }

    private static void startExport(int tabIndex, String filter) {
        if (isExporting.get()) return;

        wasPaused.set(PacketLogger.isPaused());

        PacketLogger.setPaused(true);

        isExporting.set(true);

        new Thread(() -> {
            try {
                export(tabIndex, filter);
            } finally {
                PacketLogger.setPaused(wasPaused.get());
                isExporting.set(false);
            }
        }, "PacketLog-Exporter").start();
    }

    private static void export(int tabIndex, String filter) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = "packetlog_" + tabs[tabIndex] + "_" + timestamp + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Packet Logger Export - " + tabs[tabIndex] + " Packets\n");
            writer.write("Generated at: " + new Date() + "\n");
            writer.write("Filter: " + (filter.isEmpty() ? "none" : filter) + "\n");
            writer.write("Max Depth: " + maxDepth[0] + "\n");
            writer.write("==================================================\n\n");

            int count = 0;
            for (PacketEntry entry : PacketLogger.getSnapshot()) {
                if (!matchesTab(entry, tabIndex)) continue;

                String name = PacketNameMapper.NAME_MAP.getOrDefault(
                        entry.packet.getClass(),
                        entry.packet.getClass().getSimpleName()
                );

                if (!name.toLowerCase().contains(filter.toLowerCase())) continue;

                count++;
                writer.write("Packet #" + count + "\n");
                writer.write("Name:    " + name + "\n");
                writer.write("Class:   " + entry.packet.getClass().getName() + "\n");
                writer.write("Type:    " + entry.direction + "\n");
                writer.write("Time:    " + entry.timestamp + "\n");
                writer.write("toString(): " + entry.packet + "\n");
                writer.write("Fields:\n");

                ReflectionUtils.writeObjectTreeText(writer, "", entry.packet, 0);

                writer.write("\n");
                writer.write("─".repeat(80) + "\n\n");

                if (count % 10 == 0) {
                    writer.flush();
                }
            }

            writer.write("\nTotal packets exported: " + count + "\n");
            writer.write("Export completed successfully\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean matchesTab(PacketEntry entry, int tabIndex) {
        if (tabIndex == 0) return true;
        if (tabIndex == 1) return entry.direction == Direction.OUTGOING;
        return entry.direction == Direction.INCOMING;
    }
}