package edu.library.domain;

import edu.library.exception.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Our own generic collection: a registry of uniquely identified objects.
 * @param <T> any type with a string id
 */
public class Registry<T extends Identifiable> {
    private final String kindName;
    private final List<T> entries = new ArrayList<>();

    public Registry(String kindName) {
        this.kindName = kindName;
    }

    /** @return false if an entry with the same id already exists */
    public boolean add(T entry) {
        if (findById(entry.getId()).isPresent()) {
            return false;
        }
        entries.add(entry);
        return true;
    }

    public Optional<T> findById(String id) {
        return entries.stream().filter(e -> e.getId().equalsIgnoreCase(id)).findFirst();
    }

    public T getById(String id) throws EntityNotFoundException {
        return findById(id).orElseThrow(() -> new EntityNotFoundException(kindName, id));
    }

    public List<T> findAll(Predicate<T> filter) {
        return entries.stream().filter(filter).toList();
    }

    /** Returns a sorted copy; the registry itself keeps insertion order. */
    public List<T> sorted(Comparator<? super T> comparator) {
        List<T> copy = new ArrayList<>(entries);
        copy.sort(comparator);
        return copy;
    }

    public List<T> all() {
        return List.copyOf(entries);
    }

    public int size() {
        return entries.size();
    }
}
