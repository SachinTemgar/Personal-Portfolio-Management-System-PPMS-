@SuppressWarnings("unchecked")
public class ArrayStackImpl<T> implements StackADT<T> {
    private T[] stack;
    private int top;
    private static final int DEFAULT_CAPACITY = 10;

    public ArrayStackImpl() {
        stack = (T[]) new Object[DEFAULT_CAPACITY];
        top = 0;
    }

    @Override
    public void push(T item) {
        if (top == stack.length) {
            resize();
        }
        stack[top++] = item;
    }

    @Override
    public T pop() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        T item = stack[--top];
        stack[top] = null;
        return item;
    }

    @Override
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Stack is empty");
        }
        return stack[top - 1];
    }

    @Override
    public boolean isEmpty() {
        return top == 0;
    }

    @Override
    public int size() {
        return top;
    }

    @Override
    public void clear() {
        while (!isEmpty()) {
            pop();
        }
    }

    private void resize() {
        T[] newStack = (T[]) new Object[stack.length * 2];
        System.arraycopy(stack, 0, newStack, 0, stack.length);
        stack = newStack;
    }
}
