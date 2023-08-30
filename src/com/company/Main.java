package com.company;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Main {

    private static final String letters = "RLRFR";
    private static final int length = 100;
    private static final char symbol = 'R';
    private static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            threads.add(new Thread(() -> {
                String route = generateRoute(letters, length);
                int count = findSymbol(symbol, route);
                updateFreqSize(count);
            }));
            threads.get(i).start();
        }
        var sortedMap = sizeToFreq.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .toArray(Map.Entry[]::new);
        var maxKey = sortedMap[sortedMap.length - 1].getKey();
        System.out.println(String.format("Самое частое повторение %s (встретилось %d раз)", maxKey, sizeToFreq.get(maxKey)));
        System.out.println("Другие размеры:");
        Iterator<Map.Entry<Integer, Integer>> iterator = sizeToFreq.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();
            if (maxKey.equals(entry.getKey())) {
                continue;
            }
            System.out.println(String.format("- %d (%d раз)", entry.getKey(), entry.getValue()));
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
    }
}
