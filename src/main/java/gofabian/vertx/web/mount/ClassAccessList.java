package gofabian.vertx.web.mount;

import java.util.LinkedList;
import java.util.List;

public class ClassAccessList<T> {

    private final LinkedList<T> list = new LinkedList<>();

    public ClassAccessList(List<T> list) {
        this.list.addAll(list);
    }

    public List<T> getList() {
        return list;
    }

    public ClassAccessList<T> addFirst(T element) {
        list.addFirst(element);
        return this;
    }

    public ClassAccessList<T> addLast(T element) {
        list.addLast(element);
        return this;
    }

    public ClassAccessList<T> addBefore(Class<? extends T> clazz, T element) {
        int index = indexOf(clazz);
        list.add(index, element);
        return this;
    }

    public ClassAccessList<T> addAfter(Class<? extends T> clazz, T element) {
        int index = indexOf(clazz);
        list.add(index + 1, element);
        return this;
    }

    public ClassAccessList<T> replace(Class<? extends T> clazz, T element) {
        int index = indexOf(clazz);
        list.remove(index);
        list.add(index, element);
        return this;
    }

    public ClassAccessList<T> removeFirst() {
        list.removeFirst();
        return this;
    }

    public ClassAccessList<T> removeLast() {
        list.removeLast();
        return this;
    }

    public ClassAccessList<T> remove(Class<? extends T> clazz) {
        int index = indexOf(clazz);
        list.remove(index);
        return this;
    }

    private int indexOf(Class<? extends T> clazz) {
        for (int i = 0; i < list.size(); i++) {
            if (clazz.isInstance(list.get(i))) {
                return i;
            }
        }
        throw new IllegalArgumentException("Unknown type: " + clazz);
    }

}
