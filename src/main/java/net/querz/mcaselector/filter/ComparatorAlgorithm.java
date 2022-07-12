package net.querz.mcaselector.filter;

import net.querz.nbt.CompoundTag;
import net.querz.nbt.ListTag;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Function;

@FunctionalInterface
public interface ComparatorAlgorithm {

    // XXX 'apply' instead of 'compare'?
    <C, V> boolean compare(Collection<C> container, Collection<V> values, BiPredicate<C, V> matcher);

    /*
        SOON comparator usage implementations contain a lot of duplication, but this probably isn't the optimal way to solve it
            Connect algorithm instance to Comparator enum?
            Replace abstract methods of filters with a name implying a comparator with a "getValue"-method, moving algorithm handling even further upstream?
     */

    // Exposing these methods through a ComparatorAlgorithm-typed field tags on the default methods
    ComparatorAlgorithm CONTAINS = ComparatorAlgorithm::contains;
    ComparatorAlgorithm INTERSECTS = ComparatorAlgorithm::intersects;
    ComparatorAlgorithm EQUALS = ComparatorAlgorithm::equals;

    /*
        XXX using default methods feels kind of dirty, use abstract class instead?
            Not as clean when referencing though (unless functional constructor parameter?)
     */

    default <C, V> boolean compare(Collection<C> container, Collection<V> values, Function<C, V> mapper) {
        // container is (usually) iterated for every iteration of values, so caching mappings is beneficial
        Collection<V> mappedContainer = container.stream().map(mapper).toList();
        return compare(mappedContainer, values, Object::equals);
    }

    default <T> boolean compare(Collection<T> container, Collection<T> values) {
        return compare(container, values, Object::equals);
    }

    /** Returns true if ALL elements of values are in any of compoundTagList's elements at the given key */
    default boolean compareFromCompoundList(Collection<CompoundTag> compoundTagList, String key, Collection<String> values) {
        return compare(compoundTagList, values, (tag) -> tag.getString(key));
    }

    default boolean compareFromCompoundList(ListTag compoundTagList, String key, Collection<String> values) {
        return compare(compoundTagList, values, (tag) -> ((CompoundTag) tag).getString(key));
    }


    /** Returns true if ALL elements of values are in container */
    private static <C, V> boolean contains(Collection<C> container, Collection<V> values, BiPredicate<C, V> matcher) {
        if (values.size() > container.size()) {
            return false;
        }

        // OPTIMIZE could this be meaningfully optimized by checking if container items are in values instead, returning early if there's not enough container items remaining?

        valueLoop:
        for (V value : values) {
            for (C containerValue : container) {
                if (matcher.test(containerValue, value)) {
                    continue valueLoop;
                }
            }
            return false;
        }
        return true;
    }

    /** Returns true if ANY elements of values are in container */
    private static <C, V> boolean intersects(Collection<C> container, Collection<V> values, BiPredicate<C, V> matcher) {
        for (V value : values) {
            for (C containerValue : container) {
                if (matcher.test(containerValue, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Returns true if ALL elements of A are in B, and ALL elements of B are in A */
    private static <T, U> boolean equals(Collection<T> a, Collection<U> b, BiPredicate<T, U> matcher) {
        if (a.size() != b.size()) {
            return false;
        }

        // because A and B are equal in size, if everything of B is in A, A can't have extraneous elements
        return contains(a, b, matcher);
    }

}
