package com.afollestad.aimage;

import java.util.*;

/**
 * @author Aidan Follestad
 */
public class QueueMap<T> implements Map<String, QueueMap.MultiMapValue<T>> {

    public QueueMap() {
        keys = new ArrayList<String>();
        values = new ArrayList<T>();
    }

    private List<String> keys;
    private List<T> values;

    @Override
    public void clear() {
        keys.clear();
        values.clear();
    }

    @Override
    public boolean containsKey(Object o) {
        for(int i = 0; i < keys.size(); i++) {
            if(keys.get(i).equals(o))
                return true;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object o) {
        return values.contains(o);
    }

    @Override
    public Set<Entry<String, MultiMapValue<T>>> entrySet() {
        throw new IllegalAccessError("entrySet() is not supported by the QueueMap class.");
    }

    @Override
    public MultiMapValue<T> get(Object o) {
        return get(o, false);
    }

    public MultiMapValue<T> get(Object o, boolean remove) {
        ArrayList<T> toreturn = new ArrayList<T>();
        for(int i = 0; i < keys.size(); i++) {
            if(keys.get(i).equals(o)) {
                if(remove) {
                    keys.remove(i);
                    values.remove(i);
                }
                toreturn.add(values.get(i));
            }
        }
        return new MultiMapValue((String)o, toreturn);
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty() || values.isEmpty();
    }

    @Override
    public Set<String> keySet() {
        Set<String> set = new HashSet<String>();
        for(int i = 0; i < keys.size(); i++)
            set.add(keys.get(i));
        return set;
    }

    public void add(String key, T value) {
        keys.add(key);
        values.add(value);
    }

    @Override
    public MultiMapValue put(String s, MultiMapValue<T> multiMapValue) {
        for(T value : multiMapValue.getItems())
            add(s, value);
        return multiMapValue;
    }

    @Override
    public void putAll(Map<? extends String, ? extends MultiMapValue<T>> map) {
        throw new IllegalAccessError("putAll() is not supported by the QueueMap class.");
    }

    @Override
    public MultiMapValue<T> remove(Object o) {
        MultiMapValue<T> toreturn = new MultiMapValue<T>((String)o);
        for(int i = 0; i < keys.size(); i++) {
            if(keys.get(i).equals(o)) {
                toreturn.add(values.get(i));
                keys.remove(i);
                values.remove(i);
            }
        }
        return toreturn;
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public Collection<MultiMapValue<T>> values() {
        Set<MultiMapValue<T>> set = new HashSet<MultiMapValue<T>>();
        for(int i = 0; i < keys.size(); i++)
            set.add(get(keys.get(i)));
        return set;
    }


    public static class MultiMapValue<T> implements Iterable<T> {

        public MultiMapValue(String key) {
            this.key = key;
            items = new ArrayList<T>();
        }
        public MultiMapValue(String key, ArrayList<T> values) {
            this(key);
            for(T val : values)
                items.add(val);
        }

        private String key;
        private List<T> items;

        public String getKey() {
            return key;
        }

        public void add(T value) {
            items.add(value);
        }

        public void add(int index, T value) {
            items.add(index, value);
        }

        public void remove(int index) {
            items.remove(index);
        }

        public void remove(T object) {
            items.remove(object);
        }

        public int size() {
            return items.size();
        }

        public void clear() {
            items.clear();
        }

        public List<T> getItems() {
            return items;
        }

        @Override
        public Iterator<T> iterator() {
            Set<T> set = new HashSet<T>();
            for(T val : getItems())
                set.add(val);
            return set.iterator();
        }
    }
}
