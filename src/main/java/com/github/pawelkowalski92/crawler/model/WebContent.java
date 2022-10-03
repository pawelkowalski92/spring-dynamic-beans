package com.github.pawelkowalski92.crawler.model;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public interface WebContent<T> {

    HttpStatus status();

    Instant retrievedAt();

    T content();

    boolean isSuccessful();

    String error();

    record Success<T>(HttpStatus status, Instant retrievedAt, T content) implements WebContent<T> {

        @Override
        public boolean isSuccessful() {
            return true;
        }

        @Override
        public String error() {
            return null;
        }

    }

    record Failure<T>(HttpStatus status, Instant retrievedAt, String error) implements WebContent<T> {

        @Override
        public T content() {
            return null;
        }

        @Override
        public boolean isSuccessful() {
            return false;
        }

    }

}
