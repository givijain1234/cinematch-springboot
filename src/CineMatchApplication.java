package com.cinematch;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

// --- MODELS ---
record Seat(String id, String vibe, boolean isBooked, double price) {}

// --- SERVICE ---
@Service
class BookingService {
    private final Map<String, Seat> seats = new ConcurrentHashMap<>();
    private final Map<String, ReentrantLock> seatLocks = new ConcurrentHashMap<>();
    private final double BASE_PRICE = 15.0;

    public BookingService() {
        for (char row = 'A'; row <= 'C'; row++) {
            for (int i = 1; i <= 5; i++) {
                String id = row + String.valueOf(i);
                seats.put(id, new Seat(id, "Empty", false, BASE_PRICE));
                seatLocks.put(id, new ReentrantLock());
            }
        }
    }

    public synchronized void printASCIIBoard() {
        System.out.println("\n--- 📽️ LIVE THEATER MAP ---");
        seats.forEach((id, seat) -> {
            String display = seat.isBooked() ? "[ X ]" : "[" + id + "]";
            System.out.print(display + " ");
            if (id.endsWith("5")) System.out.println();
        });
    }

    public String bookMultiple(List<String> ids, String vibe) {
        List<String> success = new ArrayList<>();
        // Sort IDs to prevent "Deadlocks" during multi-seat booking
        Collections.sort(ids);

        for (String id : ids) {
            ReentrantLock lock = seatLocks.get(id);
            if (lock != null && lock.tryLock()) {
                try {
                    if (!seats.get(id).isBooked()) {
                        seats.put(id, new Seat(id, vibe, true, BASE_PRICE));
                        success.add(id);
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
        return success.isEmpty() ? "❌ All seats taken!" : "✅ Booked: " + success;
    }
}

// --- CONTROLLER ---
@RestController
@RequestMapping("/api/cinema")
class CinemaController {
    private final BookingService service;
    public CinemaController(BookingService service) { this.service = service; }

    @PostMapping("/squad-book")
    public String squad(@RequestParam List<String> ids, @RequestParam String vibe) {
        return service.bookMultiple(ids, vibe);
    }
}

// --- DYNAMIC INPUT (Command Line Interface) ---
@SpringBootApplication
public class CineMatchApplication implements CommandLineRunner {
    private final BookingService service;
    public CineMatchApplication(BookingService service) { this.service = service; }

    public static void main(String[] args) {
        SpringApplication.run(CineMatchApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("🎬 CineMatch Social Engine Started!");

        while (true) {
            service.printASCIIBoard();
            System.out.println("\nAdmin Console: 1. Quick Book | 2. Show Map | 3. Exit");
            String cmd = scanner.nextLine();

            if (cmd.equals("1")) {
                System.out.print("Enter Seat ID: ");
                String id = scanner.nextLine().toUpperCase();
                System.out.println(service.bookMultiple(List.of(id), "AdminOverride"));
            } else if (cmd.equals("3")) {
                System.exit(0);
            }
        }
    }
}