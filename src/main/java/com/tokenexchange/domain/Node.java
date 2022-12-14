package com.tokenexchange.domain;

import com.tokenexchange.infrastructure.exception.ErrorType;
import com.tokenexchange.infrastructure.exception.ParameterizedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class Node<T> {

    private T value;
    private List<Node<T>> neighbours;

    public Node(T value) {
        this.value = value;
        this.neighbours = new ArrayList<>();
    }

    public T getValue() {
        return value;
    }

    public List<Node<T>> getNeighbours() {
        return new ArrayList<>(neighbours);
    }

    public void connect(Node<T> node) {
        if (this == node){
            throw new ParameterizedException(ErrorType.INTERNAL_ERROR);
        }
        this.neighbours.add(node);
        node.neighbours.add(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Node)) return false;
        Node<?> node = (Node<?>) o;
        return Objects.equals(value, node.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
                .add("value=" + value)
                .toString();
    }
}
