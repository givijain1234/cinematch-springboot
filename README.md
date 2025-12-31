# 🎬 CineMatch: Social Cinema Booking System

### 🚩 The "Why" (Use Case)
Imagine a high-demand movie premiere where thousands of fans hit the "Book" button simultaneously. 
- **The Problem:** Without thread safety, the system might sell the same seat (A1) to five different people (The "Double Booking" problem).
- **The Solution:** CineMatch uses **Spring Boot** and **Fine-Grained Reentrant Locks** to ensure that every seat is a protected resource. Only one user can "win" the race for a specific seat, while others are safely notified.

### 🛠️ Tech Stack
* **Framework:** Spring Boot 3.x (REST API)
* **Language:** Java 17+
* **Concurrency:** `ReentrantLock`, `ConcurrentHashMap`, `AtomicInteger`
* **Interface:** Dual-mode (REST API + Interactive Admin Terminal)

### 💡 Core Engineering Highlights
* **Fine-Grained Locking:** We implement locking on a *per-seat* basis. This ensures that booking seat **A1** does not block someone else from booking seat **C5**, maximising system throughput.
* **Dynamic Surge Pricing:** The system monitors theater occupancy. Once the "House Full" threshold (exceeding 50%) is reached, prices dynamically adjust by 20% to simulate real-world supply/demand logic.
* **Deadlock Prevention:** For group bookings, the system sorts seat IDs before acquiring locks, preventing circular wait conditions.

### 🧪 How to Run & Use (Example Flow)

#### 1. Start the Application
Run `CineMatchApplication.java`. You will see the **Live Admin Map** appear in your terminal.

#### 2. Book a Seat (Via API)
Open your browser or Postman and execute a POST request:
`http://localhost:8080/api/cinema/reserve?id=B2&vibe=PopcornSharer`

**Expected JSON Response:**
```json
"🎉 Success! Seat: B2 | Vibe: PopcornSharer | Price: $15.00 | Message: Enjoy the show!"
