package com.cinematch;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

// --- DATA MODEL ---
class Seat {
    public String id;
    public boolean isBooked = false;
    public String hostedVibe = "Empty";
    public final ReentrantLock lock = new ReentrantLock();
    public double basePrice = 15.0;

    public Seat(String id) {
        this.id = id;
    }
}

// --- BUSINESS LOGIC LAYER ---
@Service
class BookingService {
    private final Map<String, Seat> seats = new ConcurrentHashMap<>();
    private final AtomicInteger totalBooked = new AtomicInteger(0);
    private final int capacity = 15;

    public BookingService() {
        String[] rows = {"A", "B", "C"};
        for (String r : rows) {
            for (int i = 1; i <= 5; i++) {
                String id = r + i;
                seats.put(id, new Seat(id));
            }
        }
    }

    public Map<String, Seat> getAllSeats() {
        return seats;
    }

    public String bookMultiple(List<String> seatIds, String vibe) {
        // Defrost the list to allow sorting (prevents Deadlocks)
        List<String> sortedIds = new ArrayList<>(seatIds);
        Collections.sort(sortedIds);

        List<ReentrantLock> locksToAcquire = new ArrayList<>();
        for (String id : sortedIds) {
            Seat seat = seats.get(id);
            if (seat == null) return "🚫 Error: Seat " + id + " does not exist.";
            locksToAcquire.add(seat.lock);
        }

        // Acquire locks in sorted order
        for (ReentrantLock lock : locksToAcquire) {
            lock.lock();
        }

        try {
            for (String id : sortedIds) {
                if (seats.get(id).isBooked) {
                    return "🚫 Denied! One or more seats are already taken.";
                }
            }

            double currentPrice = (totalBooked.get() > capacity / 2) ? 18.0 : 15.0;

            for (String id : sortedIds) {
                Seat seat = seats.get(id);
                seat.isBooked = true;
                seat.hostedVibe = vibe;
                totalBooked.incrementAndGet();
            }
            return "🎉 Success! Booked " + sortedIds + " for vibe: " + vibe + " at $" + currentPrice;
        } finally {
            // Release locks in reverse order
            for (int i = locksToAcquire.size() - 1; i >= 0; i--) {
                locksToAcquire.get(i).unlock();
            }
        }
    }
}

// --- API CONTROLLER ---
@RestController
@RequestMapping("/api/cinema")
class CinemaController {
    private final BookingService bookingService;

    public CinemaController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/view")
    public Map<String, Seat> view() {
        return bookingService.getAllSeats();
    }

    @GetMapping("/reserve") // Changed from PostMapping to GetMapping for easy browser testing
    public String reserve(@RequestParam String id, @RequestParam String vibe) {
        return bookingService.bookMultiple(Collections.singletonList(id), vibe);
    }
}

// --- MAIN APPLICATION & ADMIN CONSOLE ---
@SpringBootApplication
public class CineMatchApplication implements CommandLineRunner {
    private final BookingService bookingService;

    public CineMatchApplication(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public static void main(String[] args) {
        SpringApplication.run(CineMatchApplication.class, args);
    }

    @Override
    public void run(String... args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("\n🎬 CineMatch Social Engine Started!");

        while (true) {
            displayMap();
            System.out.print("\nAdmin Console: 1. Quick Book | 2. Show Map | 3. Exit\nAction > ");
            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                System.out.print("Enter Seat ID: ");
                String id = scanner.nextLine().toUpperCase();
                System.out.println(bookingService.bookMultiple(Collections.singletonList(id), "AdminChoice"));
            } else if (choice.equals("3")) {
                System.exit(0);
            }
        }
    }

    private void displayMap() {
        System.out.println("\n--- 📽️ LIVE THEATER MAP ---");
        Map<String, Seat> seats = bookingService.getAllSeats();
        List<String> keys = new ArrayList<>(seats.keySet());
        Collections.sort(keys);

        int count = 0;
        for (String key : keys) {
            Seat s = seats.get(key);
            System.out.print("[" + (s.isBooked ? " X " : s.id) + "] ");
            if (++count % 5 == 0) System.out.println();
        }
    }
}