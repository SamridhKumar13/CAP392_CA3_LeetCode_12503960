abstract class AbstractCache {
    protected final int capacity;

    protected AbstractCache(int capacity) {
        this.capacity = capacity;
    }

    public abstract int get(int key);
    public abstract void put(int key, int value);
}

class LFUCache extends AbstractCache {
    private static class Node {
        final int key;
        int value;
        int freq;
        Node prev, next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
            this.freq = 1;
        }
    }

    private static class DoublyLinkedList {
        final Node head;
        final Node tail;
        int size;

        DoublyLinkedList() {
            this.head = new Node(0, 0);
            this.tail = new Node(0, 0);
            this.head.next = tail;
            this.tail.prev = head;
            this.size = 0;
        }

        void addFirst(Node node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
            size++;
        }

        void remove(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            size--;
        }

        Node removeLast() {
            if (size == 0) return null;
            Node last = tail.prev;
            remove(last);
            return last;
        }
    }

    private final Map<Integer, Node> nodeMap;
    private final Map<Integer, DoublyLinkedList> freqMap;
    private int minFreq;

    public LFUCache(int capacity) {
        super(capacity);
        this.nodeMap = new HashMap<>();
        this.freqMap = new HashMap<>();
        this.minFreq = 0;
    }

    @Override
    public int get(int key) {
        if (!nodeMap.containsKey(key)) return -1;
        Node node = nodeMap.get(key);
        updateFrequency(node);
        return node.value;
    }

    @Override
    public void put(int key, int value) {
        if (capacity <= 0) return;

        if (nodeMap.containsKey(key)) {
            Node node = nodeMap.get(key);
            node.value = value;
            updateFrequency(node);
            return;
        }

        if (nodeMap.size() == capacity) {
            DoublyLinkedList minList = freqMap.get(minFreq);
            Node evicted = minList.removeLast();
            if (evicted != null) {
                nodeMap.remove(evicted.key);
            }
        }

        Node newNode = new Node(key, value);
        nodeMap.put(key, newNode);
        minFreq = 1;
        freqMap.computeIfAbsent(1, k -> new DoublyLinkedList()).addFirst(newNode);
    }

    private void updateFrequency(Node node) {
        int oldFreq = node.freq;
        DoublyLinkedList oldList = freqMap.get(oldFreq);
        oldList.remove(node);

        if (oldFreq == minFreq && oldList.size == 0) {
            minFreq++;
        }

        node.freq++;
        freqMap.computeIfAbsent(node.freq, k -> new DoublyLinkedList()).addFirst(node);
    }
}