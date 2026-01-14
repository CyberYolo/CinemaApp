package cinema.backend.util;

import cinema.backend.exception.TooManyRequestsException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class RateLimiter {

    private static final Map<String, Deque<Instant>> REQUESTS = new ConcurrentHashMap<>();


    public static void checkRate(String key, int maxRequests, Duration window) {
        Instant now = Instant.now();
        Deque<Instant> deque = REQUESTS.computeIfAbsent(key, k -> new ArrayDeque<>());

        synchronized (deque) {

            Instant cutoff = now.minus(window);
            while (!deque.isEmpty() && deque.peekFirst().isBefore(cutoff)) {
                deque.removeFirst();
            }

            if (deque.size() >= maxRequests) {
                throw new TooManyRequestsException("Too many requests, please slow down.");
            }

            deque.addLast(now);
        }
    }

    private RateLimiter() {

    }
}
