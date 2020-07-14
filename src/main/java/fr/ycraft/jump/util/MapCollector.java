package fr.ycraft.jump.util;

import fr.ycraft.jump.entity.Jump;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

public class MapCollector<T, K,V> implements Collector<T, HashMap<K, V>, Map<K, V>> {
    private final Consumer<K> duplicateKeyException;
    private final Function<T, K> keyExtractor;
    private final Function<T,V> valueExtractor;
    private final Set<Characteristics> characteristics;

    public MapCollector(Function<T, K> keyExtractor, Function<T, V> valueExtractor, Consumer<K> duplicateKeyException, Set<Characteristics> characteristics) {
        this.duplicateKeyException = duplicateKeyException;
        this.characteristics = characteristics;
        this.valueExtractor = valueExtractor;
        this.keyExtractor = keyExtractor;
    }

    @Override
    public Supplier<HashMap<K, V>> supplier() {
        return HashMap::new;
    }

    @Override
    public BiConsumer<HashMap<K, V>, T> accumulator() {
        return (map, o) -> {
            K key = this.keyExtractor.apply(o);
            if (map.putIfAbsent(key, this.valueExtractor.apply(o)) != null) this.duplicateKeyException.accept(key);
        };
    }

    @Override
    public BinaryOperator<HashMap<K, V>> combiner() {
        return (m1, m2) -> {
            for (Map.Entry<K,V> e : m2.entrySet()) {
                K k = e.getKey();
                V v = Objects.requireNonNull(e.getValue());
                V u = m1.putIfAbsent(k, v);
                if (u != null) this.duplicateKeyException.accept(k);
            }
            return m1;
        };
    }

    @Override
    public Function<HashMap<K, V>, Map<K, V>> finisher() {
        return map -> map;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return this.characteristics;
    }

    private  static final Set<Collector.Characteristics> CH_UNORDERED_ID = Collections.unmodifiableSet(EnumSet.of(
            Collector.Characteristics.UNORDERED,
            Collector.Characteristics.IDENTITY_FINISH
    ));

    public static <T, K,V> MapCollector<T,K,V> toMap(Consumer<K> duplicateKeyException,
                                                    Function<T, K> keyExtractor,
                                                    Function<T, V> valueExtractor) {
        return new MapCollector<>(
            keyExtractor,
            valueExtractor,
            duplicateKeyException,
            CH_UNORDERED_ID
        );
    }
}
