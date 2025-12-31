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
* **Build Tool:** Maven

### 🚀 Core Engineering Features

* **Thread-Safe Reservations**: Implemented fine-grained locking using `ReentrantLock` to ensure that no two users can book the same seat simultaneously.
* **Deadlock Prevention**: Utilised a **Global Lock Ordering** strategy by sorting Seat IDs before acquisition, ensuring a consistent locking hierarchy.
* **Dynamic Pricing Engine**: Integrated atomic counter logic where ticket prices adjust dynamically based on real-time theatre occupancy.
* **RESTful API Architecture**: Developed a clean controller layer to handle seat viewing and reservations via structured JSON endpoints.
* **Interactive Admin Console**: Includes a live CLI dashboard to visualise the theatre map and perform manual administrative overrides.
* 
---

### 🧪 How to Run & Use (Example Flow)

#### Prerequisites
* Ensure you have **Java 17 or 21** installed. 
* Ensure **Maven** is installed (or use the included wrapper `./mvnw`).
  
#### 1. Start the Application
Run `CineMatchApplication.java`. You will see the **Live Admin Map** appear in your terminal.

#### 2. Book a Seat (Via API)
Open your browser or Postman and execute a POST request:
`http://localhost:8080/api/cinema/reserve?id=B2&vibe=PopcornSharer`

**Expected JSON Response:**
```json
"🎉 Success! Seat: B2 | Vibe: PopcornSharer | Price: $15.00 | Message: Enjoy the show!"
