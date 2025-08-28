import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public final class UndoableStringBuilder {
    // Originator: внутреннее состояние
    private char[] value;
    private int count;

    // Caretaker: история снимков
    private final Deque<Snapshot> history = new ArrayDeque<>();

    private static final int DEFAULT_CAPACITY = 16;

    // Memento: снимок состояния
    private static final class Snapshot {
        final char[] data;
        final int count;

        Snapshot(char[] data, int count) {
            this.data = data;
            this.count = count;
        }
    }

    public UndoableStringBuilder() {
        this.value = new char[DEFAULT_CAPACITY];
        this.count = 0;
    }

    public UndoableStringBuilder(int capacity) {
        if (capacity < 0) throw new IllegalArgumentException("capacity < 0");
        this.value = new char[capacity];
        this.count = 0;
    }

    public UndoableStringBuilder(String s) {
        if (s == null) s = "null";
        int cap = Math.max(DEFAULT_CAPACITY, s.length());
        this.value = new char[cap];
        this.count = 0;
        append(s); // заполнит буфер
        //  append уже сохранил снимок, можно очистить историю после конструктора при желании
        history.clear();
    }

    public int length() {
        return count;
    }

    public int capacity() {
        return value.length;
    }
    public UndoableStringBuilder ensureCapacity(int minCapacity) {
        ensureCapacityInternal(minCapacity);
        return this;
    }

    public UndoableStringBuilder append(String str) {
        if (str == null) str = "null";
        saveSnapshot();
        int len = str.length();
        ensureCapacityInternal(count + len);
        str.getChars(0, len, value, count);
        count += len;
        return this;
    }

    public UndoableStringBuilder append(char c) {
        saveSnapshot();
        ensureCapacityInternal(count + 1);
        value[count++] = c;
        return this;
    }

    public UndoableStringBuilder delete(int start, int end) {
        checkRange(start, end);
        if (start == end) return this;
        saveSnapshot();
        int len = end - start;
        System.arraycopy(value, end, value, start, count - end);
        count -= len;
        return this;
    }

    public UndoableStringBuilder setLength(int newLength) {
        if (newLength < 0) throw new StringIndexOutOfBoundsException(newLength);
        saveSnapshot();
        ensureCapacityInternal(newLength);
        if (count < newLength) {
            Arrays.fill(value, count, newLength, '0'); // как в JDK
        }
        count = newLength;
        return this;
    }

    public UndoableStringBuilder insert(int offset, String str) {
        if (str == null) str = "null";
        checkIndexInclusive(offset); // можно вставлять в позицию == count
        saveSnapshot();
        int len = str.length();
        ensureCapacityInternal(count + len);
        System.arraycopy(value, offset, value, offset + len, count - offset);
        str.getChars(0, len, value, offset);
        count += len;
        return this;
    }

    public UndoableStringBuilder clear() {
        if (count == 0) return this;
        saveSnapshot();
        count = 0;
        return this;
    }

    public void undo() {
        if (history.isEmpty()) return;
        Snapshot s = history.pop();
        // Восстанавливаем состояние. Для простоты используем массив снимка как рабочий.
        this.value = Arrays.copyOf(s.data, Math.max(s.data.length, DEFAULT_CAPACITY));
        this.count = s.count;
    }

    @Override
    public String toString() {
        return new String(value, 0, count);
    }

    private void saveSnapshot() {
        // копируем только используемую часть, чтобы не хранить пустую емкость
        history.push(new Snapshot(Arrays.copyOf(value, count), count));
    }

    private void ensureCapacityInternal(int minCapacity) {
        if (minCapacity - value.length > 0) {
            grow(minCapacity);
        }
    }

    private void grow(int minCapacity) {
        int oldCap = value.length;
        int newCap = oldCap * 2 + 2;
        if (newCap - minCapacity < 0) newCap = minCapacity;
        value = Arrays.copyOf(value, newCap);
    }

    private void checkRange(int start, int end) {
        if (start < 0 || start > end || end > count) {
            throw new StringIndexOutOfBoundsException(
                    "start=" + start + ", end=" + end + ", length=" + count);
        }
    }

    private void checkIndexInclusive(int index) {
        if (index < 0 || index > count) {
            throw new StringIndexOutOfBoundsException(
                    "index=" + index + ", length=" + count);
        }
    }
}
