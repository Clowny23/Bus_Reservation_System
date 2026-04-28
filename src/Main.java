import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    //ENUMS
    enum BusType { AC, NON_AC }
    enum SeatType { GENERAL, LADIES }
    enum BookingStatus { HELD, CONFIRMED, CANCELLED, TRAVELLED }

    //BUS
    static class Bus {
        int busId;
        String busNumber;
        BusType type;
        int totalSeats;
        SeatType[][] seatMap;

        Bus(int id, String number, BusType type, int rows, int cols) {
            this.busId = id;
            this.busNumber = number;
            this.type = type;
            this.totalSeats = rows * cols;
            this.seatMap = new SeatType[rows][cols];

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    seatMap[i][j] = (i == 0) ? SeatType.LADIES : SeatType.GENERAL;
                }
            }
        }

        void displaySeatLayout(Set<String> bookedSeats) {
            System.out.println("\nSeat Layout (X = booked):");
            for (int i = 0; i < seatMap.length; i++) {
                for (int j = 0; j < seatMap[i].length; j++) {
                    String seat = i + "-" + j;
                    if (bookedSeats.contains(seat)) System.out.print(" X ");
                    else System.out.print(" " + seat + " ");
                }
                System.out.println();
            }
        }
    }

    //ROUTE
    static class Route {
        int routeId;
        String source;
        String destination;
        List<String> stops;
        double distanceKm;
        double durationHrs;

        Route(int id, String src, String dest, List<String> stops, double dist, double dur) {
            this.routeId = id;
            this.source = src;
            this.destination = dest;
            this.stops = stops;
            this.distanceKm = dist;
            this.durationHrs = dur;
        }
    }

    //SCHEDULE
    static class Schedule {
        int scheduleId;
        Bus bus;
        Route route;
        Date departureTime;
        Date arrivalTime;
        double fare;

        Set<String> bookedSeats = ConcurrentHashMap.newKeySet();
        Map<String, Long> heldSeats = new ConcurrentHashMap<>();

        Schedule(int id, Bus bus, Route route, Date dep, Date arr, double fare) {
            this.scheduleId = id;
            this.bus = bus;
            this.route = route;
            this.departureTime = dep;
            this.arrivalTime = arr;
            this.fare = fare;
        }

        boolean holdSeats(List<String> seats) {
            long now = System.currentTimeMillis();

            for (String s : seats) {
                if (bookedSeats.contains(s)) return false;
                if (heldSeats.containsKey(s) && (now - heldSeats.get(s)) < 8 * 60 * 1000)
                    return false;
            }

            for (String s : seats) {
                heldSeats.put(s, now);
            }
            return true;
        }

        void confirmSeats(List<String> seats) {
            for (String s : seats) {
                heldSeats.remove(s);
                bookedSeats.add(s);
            }
        }

        int getAvailableSeats() {
            return bus.totalSeats - bookedSeats.size();
        }
    }

    //PASSENGER
    static class Passenger {
        int passengerId;
        String name;
        String phone;
        String email;
        String idProof;
        String gender;

        Passenger(int id, String name, String phone, String email, String idProof, String gender) {
            this.passengerId = id;
            this.name = name;
            this.phone = phone;
            this.email = email;
            this.idProof = idProof;
            this.gender = gender;
        }
    }

    //BOOKING
    static class Booking {
        String pnr;
        int passengerId;
        int scheduleId;
        List<String> seats;
        double fare;
        BookingStatus status;
        String boardingPass;

        Booking(String pnr, Passenger p, Schedule s, List<String> seats, double fare) {
            this.pnr = pnr;
            this.passengerId = p.passengerId;
            this.scheduleId = s.scheduleId;
            this.seats = seats;
            this.fare = fare;
            this.status = BookingStatus.CONFIRMED;
            this.boardingPass = generateBoardingPass();
        }

        String generateBoardingPass() {
            return "QR-" + pnr;
        }

        void printTicket() {
            System.out.println("\n===== BOARDING PASS =====");
            System.out.println("PNR: " + pnr);
            System.out.println("Passenger ID: " + passengerId);
            System.out.println("Seats: " + seats);
            System.out.println("Fare: " + fare);
            System.out.println("QR Code: " + boardingPass);
            System.out.println("=========================");
        }
    }

    //CANCEL POLICY
    static class CancelPolicy {
        static double calculateRefund(Date dep, double fare) {
            long hrs = (dep.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60);

            if (hrs > 24) return fare * 0.9;
            if (hrs > 12) return fare * 0.75;
            if (hrs > 6) return fare * 0.5;
            if (hrs > 1) return fare * 0.25;
            return 0;
        }
    }

    //SYSTEM
    static class SystemManager {

        List<Schedule> schedules = new ArrayList<>();
        Map<String, Booking> bookings = new HashMap<>();
        int passengerCounter = 1;

        List<Schedule> search(String src, String dest) {
            List<Schedule> res = new ArrayList<>();
            for (Schedule s : schedules) {
                if (s.route.source.equalsIgnoreCase(src)
                        && s.route.destination.equalsIgnoreCase(dest)) {
                    res.add(s);
                }
            }
            return res;
        }

        Booking book(Passenger p, Schedule s, List<String> seats) {

            if (seats.size() > 6) {
                System.out.println("Max 6 seats allowed");
                return null;
            }

            for (String seat : seats) {
                int row = Integer.parseInt(seat.split("-")[0]);
                if (row == 0 && !p.gender.equalsIgnoreCase("F")) {
                    System.out.println("Ladies seat not allowed");
                    return null;
                }
            }

            if (!s.holdSeats(seats)) {
                System.out.println("Seat just got booked");
                return null;
            }

            double fare = s.fare * seats.size();

            if (seats.size() >= 5) {
                fare *= 0.9;
            }

            String pnr = UUID.randomUUID().toString().substring(0, 8);
            s.confirmSeats(seats);

            Booking b = new Booking(pnr, p, s, seats, fare);
            bookings.put(pnr, b);

            System.out.println("Booking Confirmed | PNR: " + pnr);
            b.printTicket();

            return b;
        }

        void cancel(String pnr) {
            Booking b = bookings.get(pnr);

            if (b == null) {
                System.out.println("Invalid PNR");
                return;
            }

            Schedule s = schedules.stream()
                    .filter(x -> x.scheduleId == b.scheduleId)
                    .findFirst().orElse(null);

            if (new Date().after(s.departureTime)) {
                System.out.println("Cancellation not allowed after departure");
                return;
            }

            double refund = CancelPolicy.calculateRefund(s.departureTime, b.fare);

            b.status = BookingStatus.CANCELLED;
            s.bookedSeats.removeAll(b.seats);

            if (refund == 0) {
                System.out.println("⚠ No refund (Less than 1 hour before departure)");
            } else {
                System.out.println("Cancelled | Refund: " + refund);
            }
        }
    }

    //MAIN MENU
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        SystemManager system = new SystemManager();

        // SAMPLE DATA
        Bus bus = new Bus(1, "TN01", BusType.AC, 3, 4);
        Route route = new Route(1, "Chennai", "Bangalore",
                Arrays.asList("Vellore", "Krishnagiri"), 350, 6);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 10);

        Schedule schedule = new Schedule(1, bus, route,
                cal.getTime(),
                new Date(cal.getTimeInMillis() + 6 * 60 * 60 * 1000),
                500);

        system.schedules.add(schedule);

        while (true) {
            System.out.println("\n===== MENU =====");
            System.out.println("1. Search Bus");
            System.out.println("2. Book Ticket");
            System.out.println("3. Cancel Ticket");
            System.out.println("4. Exit");
            System.out.print("Choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {

                case 1:
                    System.out.print("Source: ");
                    String src = sc.nextLine();
                    System.out.print("Destination: ");
                    String dest = sc.nextLine();

                    List<Schedule> res = system.search(src, dest);

                    if (res.isEmpty()) {
                        System.out.println("No buses available");
                    } else {
                        for (Schedule s : res) {
                            System.out.println("ScheduleID: " + s.scheduleId +
                                    " Fare: " + s.fare +
                                    " Seats: " + s.getAvailableSeats());
                        }
                    }
                    break;

                case 2:
                    System.out.print("Name: ");
                    String name = sc.nextLine();
                    System.out.print("Gender(M/F): ");
                    String gender = sc.nextLine();

                    Passenger p = new Passenger(system.passengerCounter++,
                            name, "9999999999", "mail@test.com", "ID123", gender);

                    System.out.print("Schedule ID: ");
                    int sid = sc.nextInt();

                    Schedule sel = system.schedules.stream()
                            .filter(x -> x.scheduleId == sid)
                            .findFirst().orElse(null);

                    if (sel == null) {
                        System.out.println("nvalid Schedule");
                        break;
                    }

                    sel.bus.displaySeatLayout(sel.bookedSeats);

                    System.out.print("Seats count: ");
                    int n = sc.nextInt();

                    List<String> seats = new ArrayList<>();
                    System.out.println("Enter seats (row-col): ");
                    for (int i = 0; i < n; i++) {
                        seats.add(sc.next());
                    }

                    system.book(p, sel, seats);
                    break;

                case 3:
                    System.out.print("Enter PNR: ");
                    system.cancel(sc.next());
                    break;

                case 4:
                    System.out.println("Exit");
                    return;
            }
        }
    }
}