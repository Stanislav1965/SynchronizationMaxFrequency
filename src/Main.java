import java.util.*;

public class Main {

    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    private static final int COUNT_THEAD = 1_000;

    public static void main(String[] args) throws InterruptedException {

        String[] routes = new String[COUNT_THEAD];
        List<Thread> threads = new ArrayList<>(); // Создаем список потоков

        for (int i = 0; i < COUNT_THEAD; i++) {
            routes[i] = generateRoute("RLRFR", 100);
        }

        Runnable searchMax = () -> {
            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    var max = sizeToFreq.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .orElseThrow();
                    System.out.printf("Текущее самое частое количество повторений %d  (встретилось %d раз) \n", max.getKey(), max.getValue());
                }
            }
        };

        Thread threadMaxFreq = new Thread(searchMax);
        threadMaxFreq.start();

        for (String route : routes) {
            Runnable search = () -> {
                long count = route.chars().filter(ch -> ch == 'R').count();
                long cntFreq = sizeToFreq.containsKey(Math.toIntExact(count)) ? sizeToFreq.get(Math.toIntExact(count)).longValue() : 0;
                synchronized (sizeToFreq) {
                    sizeToFreq.put(Math.toIntExact(count), (int) (cntFreq + 1));
                    sizeToFreq.notify();
                }
            };

            Thread thread = new Thread(search); // Создаем поток
            threads.add(thread); //Добавляем поток в список потоков
            thread.start(); // Запускаем поток
        }

        for (Thread thread : threads) {
            thread.join(); //  ждём когда поток объект которого лежит в thread завершится
        }

        threadMaxFreq.interrupt();

        var max = sizeToFreq.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow();
        System.out.printf("Самое частое количество повторений %d  (встретилось %d раз) \n", max.getKey(), max.getValue());

        System.out.println("Другие размеры:");
        sizeToFreq.entrySet().stream()
                .filter(item -> !item.equals(max))
                .forEach(item -> System.out.printf("- %d  (%d раз) \n", item.getKey(), item.getValue()));
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}

