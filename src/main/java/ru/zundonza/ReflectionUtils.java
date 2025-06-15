package ru.zundonza;

import imgui.ImGui;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

public class ReflectionUtils {

    private static int maxDepth = 4;

    public static void setMaxDepth(int depth) {
        maxDepth = Math.max(1, Math.min(10, depth));
    }

    public static void renderObjectTree(String nodeId, Object obj, int depth) {
        renderObjectTree(nodeId, obj, depth, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static void renderObjectTree(String nodeId, Object obj, int depth, Set<Object> visited) {
        if (obj == null) {
            ImGui.text(nodeId + ": null");
            return;
        }
        if (depth > maxDepth) {
            ImGui.text(nodeId + ": (max depth reached)");
            return;
        }

        if (isComplexObject(obj)) {
            if (visited.contains(obj)) {
                ImGui.text(nodeId + ": [circular reference]");
                return;
            }
            visited.add(obj);
        }

        Class<?> cls = obj.getClass();

        if (isSimpleType(obj)) {
            ImGui.text(nodeId + ": " + obj);
            return;
        }

        if (cls.isArray()) {
            renderArray(nodeId, obj, depth, visited);
            return;
        }

        if (obj instanceof Collection<?>) {
            renderCollection(nodeId, (Collection<?>) obj, depth, visited);
            return;
        }

        if (obj instanceof Map<?, ?>) {
            renderMap(nodeId, (Map<?, ?>) obj, depth, visited);
            return;
        }

        renderFields(nodeId, obj, depth, visited, cls);
    }

    private static boolean isSimpleType(Object obj) {
        return obj instanceof String ||
                obj instanceof Number ||
                obj instanceof Boolean ||
                obj instanceof Character ||
                obj instanceof Enum;
    }

    private static boolean isComplexObject(Object obj) {
        return !isSimpleType(obj) &&
                !obj.getClass().isPrimitive() &&
                !obj.getClass().isArray() &&
                !(obj instanceof Collection<?>) &&
                !(obj instanceof Map<?, ?>);
    }

    private static void renderArray(String nodeId, Object obj, int depth, Set<Object> visited) {
        int length = Array.getLength(obj);
        if (ImGui.treeNode(nodeId + " [Array length=" + length + "]")) {
            try {
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(obj, i);
                    renderObjectTree(nodeId + "[" + i + "]", element, depth + 1, visited);
                }
            } finally {
                ImGui.treePop();
            }
        }
    }

    private static void renderCollection(String nodeId, Collection<?> coll, int depth, Set<Object> visited) {
        if (ImGui.treeNode(nodeId + " [Collection size=" + coll.size() + "]")) {
            try {
                int i = 0;
                for (Object element : coll) {
                    renderObjectTree(nodeId + "[" + i + "]", element, depth + 1, visited);
                    i++;
                }
            } finally {
                ImGui.treePop();
            }
        }
    }

    private static void renderMap(String nodeId, Map<?, ?> map, int depth, Set<Object> visited) {
        if (ImGui.treeNode(nodeId + " [Map size=" + map.size() + "]")) {
            try {
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    String keyStr = entry.getKey() == null ? "null" : entry.getKey().toString();
                    renderObjectTree(nodeId + "[key=" + keyStr + "]", entry.getValue(), depth + 1, visited);
                }
            } finally {
                ImGui.treePop();
            }
        }
    }

    private static void renderFields(String nodeId, Object obj, int depth, Set<Object> visited, Class<?> cls) {
        Field[] fields = cls.getDeclaredFields();
        if (fields.length == 0) {
            ImGui.text(nodeId + ": " + obj);
            return;
        }

        if (ImGui.treeNode(nodeId + " (" + cls.getSimpleName() + ")")) {
            try {
                AccessibleObject.setAccessible(fields, true);
                for (Field f : fields) {
                    try {
                        f.setAccessible(true);
                        Object value = f.get(obj);
                        renderObjectTree(f.getName(), value, depth + 1, visited);
                    } catch (Exception e) {
                        ImGui.text(f.getName() + ": (error accessing)");
                    }
                }
            } finally {
                ImGui.treePop();
            }
        }
    }

    public static void writeObjectTreeText(BufferedWriter writer, String prefix, Object obj, int depth) throws IOException {
        writeObjectTreeText(writer, prefix, obj, depth, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private static void writeObjectTreeText(
            BufferedWriter writer,
            String prefix,
            Object obj,
            int depth,
            Set<Object> visited
    ) throws IOException {
        if (obj == null) {
            writer.write(prefix + "null\n");
            return;
        }
        if (depth > maxDepth) {
            writer.write(prefix + "(max depth reached)\n");
            return;
        }

        if (isComplexObject(obj)) {
            if (visited.contains(obj)) {
                writer.write(prefix + "[circular reference]\n");
                return;
            }
            visited.add(obj);
        }

        Class<?> cls = obj.getClass();

        if (isSimpleType(obj)) {
            writer.write(prefix + obj + "\n");
            return;
        }

        if (cls.isArray()) {
            writeArrayText(writer, prefix, obj, depth, visited);
            return;
        }

        if (obj instanceof Collection<?>) {
            writeCollectionText(writer, prefix, (Collection<?>) obj, depth, visited);
            return;
        }

        if (obj instanceof Map<?, ?>) {
            writeMapText(writer, prefix, (Map<?, ?>) obj, depth, visited);
            return;
        }

        writeFieldsText(writer, prefix, obj, depth, visited, cls);
    }

    private static void writeArrayText(
            BufferedWriter writer,
            String prefix,
            Object obj,
            int depth,
            Set<Object> visited
    ) throws IOException {
        int length = Array.getLength(obj);
        writer.write(prefix + "[Array] length=" + length + "\n");
        for (int i = 0; i < length; i++) {
            Object element = Array.get(obj, i);
            writeObjectTreeText(writer, prefix + "  [" + i + "] ", element, depth + 1, visited);
        }
    }

    private static void writeCollectionText(
            BufferedWriter writer,
            String prefix,
            Collection<?> coll,
            int depth,
            Set<Object> visited
    ) throws IOException {
        writer.write(prefix + "[Collection] size=" + coll.size() + "\n");
        int i = 0;
        for (Object element : coll) {
            writeObjectTreeText(writer, prefix + "  [" + i + "] ", element, depth + 1, visited);
            i++;
        }
    }

    private static void writeMapText(
            BufferedWriter writer,
            String prefix,
            Map<?, ?> map,
            int depth,
            Set<Object> visited
    ) throws IOException {
        writer.write(prefix + "[Map] size=" + map.size() + "\n");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String keyStr = entry.getKey() == null ? "null" : entry.getKey().toString();
            writer.write(prefix + "  Key: " + keyStr + "\n");
            writeObjectTreeText(writer, prefix + "  Value: ", entry.getValue(), depth + 1, visited);
        }
    }

    private static void writeFieldsText(
            BufferedWriter writer,
            String prefix,
            Object obj,
            int depth,
            Set<Object> visited,
            Class<?> cls
    ) throws IOException {
        Field[] fields = cls.getDeclaredFields();
        if (fields.length == 0) {
            writer.write(prefix + "(no fields) " + obj + "\n");
            return;
        }

        writer.write(prefix + "[" + cls.getSimpleName() + "]\n");
        AccessibleObject.setAccessible(fields, true);
        for (Field f : fields) {
            try {
                Object value = f.get(obj);
                writer.write(prefix + "  " + f.getName() + ": ");
                writeObjectTreeText(writer, "", value, depth + 1, visited);
            } catch (Exception e) {
                writer.write(prefix + "  " + f.getName() + ": (error accessing)\n");
            }
        }
    }

}