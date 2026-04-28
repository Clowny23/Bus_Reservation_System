# Bus_Reservation_System
# Bus Reservation System (Java)

## Problem Statement

Design and implement a Bus Reservation System that allows passengers to search for buses, select seats, make bookings, and cancel tickets. The system must ensure real-time seat availability, prevent booking conflicts, handle fare calculation, and enforce cancellation policies.

## Approach / Logic Used

The system is designed using Object-Oriented Programming principles with a modular Low-Level Design.

Key design decisions:

* Each real-world entity (Bus, Booking, Passenger, Schedule) is modeled as a class
* Concurrency is handled using ConcurrentHashMap to avoid seat booking conflicts
* Seat holding mechanism prevents race conditions by locking seats for 8 minutes
* Business rules enforced:

  * Maximum 6 seats per booking
  * Ladies seat restriction
  * Group discount (10% for 5 or more seats)
* Unique PNR generation using UUID
* Cancellation policy based on time before departure

## Low-Level Design (LLD)

### Actors

* Passenger
* Bus Operator (Admin)
* System (Schedule Manager)

### Preconditions

* Bus routes are configured with source, destination, stops, and schedule
* Fleet is registered with seat maps
* Fare is defined per route and seat type

### Trigger

Passenger searches for a bus using source, destination, and travel date

### Main Flow

1. Passenger searches using source, destination, and travel date
2. System displays available buses with fare, duration, and seat availability
3. Passenger selects a bus and preferred seats
4. System holds seats for 8 minutes
5. Passenger enters details and completes payment
6. Booking is confirmed and PNR is generated
7. Boarding pass is generated
8. On travel day, passenger boards using PNR/QR code
9. System updates seat status to TRAVELLED

### Alternate Flows

* No buses available: Suggest alternate dates
* Ladies seat selected by male passenger: Block and suggest general seats
* Group booking (≥5 seats): Apply 10% discount
* AC bus unavailable: Suggest Non-AC bus with adjusted fare

### Exceptional Flows

* Seat already booked: Prompt user to reselect
* Invalid PNR: Show "Booking not found"
* Travel date in the past: Block booking
* More than 6 seats: Restrict booking
* Cancellation after departure: Not allowed

### Postconditions

* Booking created with PNR
* Seats marked as RESERVED
* Boarding pass generated
* Revenue recorded
* Seat map updated

### Non-Functional Requirements

* Search results within 2 seconds
* Seat holding must be atomic
* PNR generation within 3 seconds
* System supports up to 100,000 bookings per day

## Class Design
## Cancellation Policy

| Time Before Departure | Refund Percentage | Cancellation Charge |
| --------------------- | ----------------- | ------------------- |
| > 24 hours            | 90%               | 10% of fare         |
| 12–24 hours           | 75%               | 25% of fare         |
| 6–12 hours            | 50%               | 50% of fare         |
| 1–6 hours             | 25%               | 75% of fare         |
| < 1 hour              | 0%                | No refund           |

## Steps to Execute the Code
1. Clone the repository:
   git clone https://github.com/Clowny23/Bus_Reservation_System.git

2. Open the project in IntelliJ IDEA or any Java IDE

3. Ensure JDK (Java 8 or above) is installed

4. Run the Main.java file

5. Use the console menu:

   * Search Bus
   * Book Ticket
   * Cancel Ticket

## Sample Features Demonstrated

* Real-time seat availability
* Seat locking mechanism (8 minutes)
* Concurrency handling using ConcurrentHashMap
* Fare calculation with discounts
* PNR-based booking system
* Cancellation with refund logic

## Future Enhancements

* Database integration (MySQL / MongoDB)
* Web or mobile interface
* Payment gateway integration
* Real-time notifications (SMS/Email)
* Admin dashboard for bus operators
