package com.dabel.service;

public interface EvaluableOperation<T> {

    void init(T t);

    void approve(T t);

    void reject(T t, String remarks);
}
