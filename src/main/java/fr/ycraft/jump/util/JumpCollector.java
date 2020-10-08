package fr.ycraft.jump.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

public class JumpCollector<T, K,V> {
    private final Consumer<K> duplicateKeyException;
    private final Function<T, K> keyExtractor;
    private final Function<T,V> valueExtractor;
    private final HashMap<K, V> collection = new HashMap<>();

    public JumpCollector(Function<T, K> keyExtractor, Function<T, V> valueExtractor, Consumer<K> duplicateKeyException) {
        this.duplicateKeyException = duplicateKeyException;
        this.valueExtractor = valueExtractor;
        this.keyExtractor = keyExtractor;
    }

    public void accumulate(T o) {
        K key = this.keyExtractor.apply(o);
        if (this.collection.putIfAbsent(key, this.valueExtractor.apply(o)) != null) this.duplicateKeyException.accept(key);
    }

    public JumpCollector<T,K,V> combine(JumpCollector<T,K,V> other) {
        for (Map.Entry<K,V> e : other.collection.entrySet()) {
            K k = e.getKey();
            V v = Objects.requireNonNull(e.getValue());
            V u = this.collection.putIfAbsent(k, v);
            if (u != null) this.duplicateKeyException.accept(k);
        }
        return this;
    }

    public List<V> finish() {
        return new ArrayList<>(this.collection.values());
    }

    public static <T, K,V> Collector<T, JumpCollector<T,K,V>, List<V>> toList(
            Consumer<K> duplicateKeyException,
            Function<T, K> keyExtractor,
            Function<T, V> valueExtractor) {
        return Collector.of(
                () -> new JumpCollector<>(keyExtractor, valueExtractor, duplicateKeyException),
                JumpCollector::accumulate,
                JumpCollector::combine,
                JumpCollector::finish,
                Collector.Characteristics.UNORDERED
        );
    }
}
