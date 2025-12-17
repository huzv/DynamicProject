package util;

import java.util.Iterator;
import java.util.Comparator;
import java.util.NoSuchElementException;

public class ArrayList<E> implements Iterable<E> {
    private static final int DEFAULT_CAPACITY = 10;
    private Object[] elements;
    private int size;

    public ArrayList() {
        this.elements = new Object[DEFAULT_CAPACITY];
        this.size = 0;
    }

    public ArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.elements = new Object[initialCapacity];
        this.size = 0;
    }

    public ArrayList(ArrayList<? extends E> other) {
        this.size = other.size;
        this.elements = new Object[other.size];
        for (int i = 0; i < other.size; i++) {
            this.elements[i] = other.elements[i];
        }
    }

    public boolean add(E element) {
        ensureCapacity(size + 1);
        elements[size++] = element;
        return true;
    }

    public void add(int index, E element) {
        rangeCheckForAdd(index);
        ensureCapacity(size + 1);

        // Shift elements to the right
        for (int i = size; i > index; i--) {
            elements[i] = elements[i - 1];
        }

        elements[index] = element;
        size++;
    }

    public boolean addAll(ArrayList<? extends E> other) {
        if (other == null) {
            throw new NullPointerException("Cannot add null collection");
        }
        if (other.isEmpty()) {
            return false;
        }

        ensureCapacity(size + other.size());
        for (int i = 0; i < other.size(); i++) {
            elements[size++] = other.get(i);
        }
        return true;
    }

    public boolean addAll(int index, ArrayList<? extends E> other) {
        rangeCheckForAdd(index);
        if (other == null) {
            throw new NullPointerException("Cannot add null collection");
        }
        if (other.isEmpty()) {
            return false;
        }

        ensureCapacity(size + other.size());

        // Shift existing elements to make room
        for (int i = size - 1; i >= index; i--) {
            elements[i + other.size()] = elements[i];
        }

        // Insert new elements
        for (int i = 0; i < other.size(); i++) {
            elements[index + i] = other.get(i);
        }

        size += other.size();
        return true;
    }

    @SuppressWarnings("unchecked")
    public E get(int index) {
        rangeCheck(index);
        return (E) elements[index];
    }

    public E set(int index, E element) {
        rangeCheck(index);
        E oldValue = get(index);
        elements[index] = element;
        return oldValue;
    }

    public E remove(int index) {
        rangeCheck(index);
        E oldValue = get(index);

        // Shift elements to the left
        for (int i = index; i < size - 1; i++) {
            elements[i] = elements[i + 1];
        }

        elements[--size] = null;
        return oldValue;
    }

    public boolean remove(Object o) {
        for (int i = 0; i < size; i++) {
            if (o == null ? elements[i] == null : o.equals(elements[i])) {
                remove(i);
                return true;
            }
        }
        return false;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    public int indexOf(Object o) {
        for (int i = 0; i < size; i++) {
            if (o == null ? elements[i] == null : o.equals(elements[i])) {
                return i;
            }
        }
        return -1;
    }

    // Capacity management
    private void ensureCapacity(int minCapacity) {
        if (minCapacity > elements.length) {
            int newCapacity = elements.length * 2;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            resize(newCapacity);
        }
    }

    private void resize(int newCapacity) {
        Object[] newElements = new Object[newCapacity];
        for (int i = 0; i < size; i++) {
            newElements[i] = elements[i];
        }
        elements = newElements;
    }

    // Sorting (using quicksort)
    public void sort(Comparator<? super E> comparator) {
        if (comparator == null) {
            throw new NullPointerException("Comparator cannot be null");
        }
        if (size > 1) {
            quickSort(0, size - 1, comparator);
        }
    }

    @SuppressWarnings("unchecked")
    private void quickSort(int low, int high, Comparator<? super E> comparator) {
        if (low < high) {
            int pivotIndex = partition(low, high, comparator);
            quickSort(low, pivotIndex - 1, comparator);
            quickSort(pivotIndex + 1, high, comparator);
        }
    }

    @SuppressWarnings("unchecked")
    private int partition(int low, int high, Comparator<? super E> comparator) {
        E pivot = (E) elements[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (comparator.compare((E) elements[j], pivot) <= 0) {
                i++;
                swap(i, j);
            }
        }

        swap(i + 1, high);
        return i + 1;
    }

    private void swap(int i, int j) {
        Object temp = elements[i];
        elements[i] = elements[j];
        elements[j] = temp;
    }

    public void reverse() {
        int left = 0;
        int right = size - 1;

        while (left < right) {
            swap(left, right);
            left++;
            right--;
        }
    }

    public Object[] toArray() {
        Object[] result = new Object[size];
        for (int i = 0; i < size; i++) {
            result[i] = elements[i];
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            return (T[]) java.util.Arrays.copyOf(elements, size, a.getClass());
        }
        for (int i = 0; i < size; i++) {
            a[i] = (T) elements[i];
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @Override
    public Iterator<E> iterator() {
        return new ArrayListIterator();
    }

    private class ArrayListIterator implements Iterator<E> {
        private int currentIndex = 0;
        private int lastReturnedIndex = -1;

        @Override
        public boolean hasNext() {
            return currentIndex < size;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            lastReturnedIndex = currentIndex;
            return (E) elements[currentIndex++];
        }

        @Override
        public void remove() {
            if (lastReturnedIndex < 0) {
                throw new IllegalStateException();
            }
            ArrayList.this.remove(lastReturnedIndex);
            currentIndex = lastReturnedIndex;
            lastReturnedIndex = -1;
        }
    }

    private void rangeCheck(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    private void rangeCheckForAdd(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < size; i++) {
            sb.append(elements[i]);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ArrayList))
            return false;

        ArrayList<?> other = (ArrayList<?>) o;
        if (size != other.size)
            return false;

        for (int i = 0; i < size; i++) {
            Object e1 = elements[i];
            Object e2 = other.elements[i];
            if (!(e1 == null ? e2 == null : e1.equals(e2))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = 0; i < size; i++) {
            Object e = elements[i];
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }
}