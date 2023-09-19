package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class FrequencyCalculator {
    private static final String letters = "RLRFR";
    private static final int length = 100;
    private static final char symbol = 'R';
    private static final Map<Integer, Integer> sizeToFreq = new HashMap<>();
    private final List<Thread> threads = new ArrayList<>();
    private final static Thread freqLog = new Thread(FrequencyCalculator::printCurrentMaxFreq,"freqLog");

    public void calculate() throws InterruptedException {
        freqLog.start();
        for (int i = 0; i < 1000; i++) {
            threads.add(new Thread(() -> {
                String route = generateRoute(letters, length);
                int count = findSymbol(symbol, route);
                updateFreqSize(count);
            }));
            threads.get(i).start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        freqLog.interrupt();
        var sortedMap = sizeToFreq.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .toArray(Map.Entry[]::new);
        Object maxKey = sortedMap[sortedMap.length - 1].getKey();
        System.out.printf("Самое частое повторение %s (встретилось %d раз)%n", maxKey, sizeToFreq.get((Integer) maxKey));
        System.out.println("Другие размеры:");
        for (int i = sortedMap.length - 1; i >= 0; i--) {
            if (maxKey.equals(sortedMap[i].getKey())) {
                continue;
            }
            System.out.printf("- %d (%d раз)%n", (int) sortedMap[i].getKey(), (int) sortedMap[i].getValue());
        }
        System.out.println();
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

    private static int findSymbol(char symbol, String route) {
        int count = 0;
        for (int i = 0; i < route.length(); i++) {
            if (route.charAt(i) == symbol) {
                count++;
            }
        }
        return count;
    }

    private static synchronized void updateFreqSize(int count) {
        if (!sizeToFreq.containsKey(count)) {
            sizeToFreq.put(count, 1);
        } else {
            int currentFreq = sizeToFreq.get(count);
            sizeToFreq.put(count, ++currentFreq);
        }
        synchronized (freqLog) {
            freqLog.notify();
        }
    }

    private static int findCurrentMaxKeyFreq() {
        int maxFreq = Integer.MIN_VALUE;
        int maxKeyFreq = Integer.MIN_VALUE;
        for (var entry : sizeToFreq.entrySet()) {
            if (entry.getValue() > maxFreq) {
                maxFreq = entry.getValue();
                maxKeyFreq = entry.getKey();
            }
        }
        return maxKeyFreq;
    }

    private static void printCurrentMaxFreq() {
        while (!Thread.interrupted()) {
            try {
                synchronized (freqLog) {
                    freqLog.wait();
                }
                int maxKeyFreq = findCurrentMaxKeyFreq();
                System.out.printf("Log: Самое частое повторение %s (встретилось %d раз)%n", maxKeyFreq, sizeToFreq.get(maxKeyFreq));
            } catch (InterruptedException e) {
                System.out.printf("Thread '%s' is interrupted", Thread.currentThread().getName());
                System.out.println();
                break;
            }
        }
    }
}
