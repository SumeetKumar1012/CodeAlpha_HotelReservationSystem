package com.hotel.system;

import com.hotel.models.User;
import com.hotel.models.Room;
import com.hotel.models.Reservation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeParseException;

import com.toedter.calendar.JDateChooser;

import java.text.SimpleDateFormat;

public class HotelReservationSystem extends JFrame {
    private ArrayList<Room> rooms;
    private ArrayList<Reservation> reservations;
    private ArrayList<User> users;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private boolean isAdmin = false;
    private static final String ADMIN_PASSWORD = "admin123";
    private static final String USERS_FILE = "users.dat";

    public HotelReservationSystem() {
        rooms = new ArrayList<>();
        reservations = new ArrayList<>();
        users = new ArrayList<>();
        loadData();

        setTitle("Hotel Reservation System");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        createLoginPanel();
        createLoginOrRegisterPanel();
        createUserPanel();
        createAdminPanel();

        add(mainPanel);
        setLocationRelativeTo(null);
    }

    private void createLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout()) {
            private Image backgroundImage;
            {
                try {
                    String imagePath = "E:\\HotelReservationSystem\\resources\\hotel_bg.jpg";
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        backgroundImage = ImageIO.read(imageFile);
                    } else {
                        System.err.println("Image file not found: " + imagePath);
                    }
                } catch (Exception e) {
                    System.err.println("Error loading background image: " + e.getMessage());
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        loginPanel.setOpaque(false);

        JPanel headingPanel = new JPanel();
        headingPanel.setBackground(new Color(0, 0, 0, 150));
        JLabel welcomeLabel = new JLabel("Welcome to Luxembor Hotel");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        welcomeLabel.setForeground(Color.WHITE);
        headingPanel.add(welcomeLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 0, 0, 0);
        loginPanel.add(headingPanel, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        buttonPanel.setOpaque(false);
        JButton userButton = createStyledButton("User Login", new Color(255, 204, 102), new Color(255, 214, 128));
        JButton adminButton = createStyledButton("Admin Login", new Color(10, 10, 10, 150), new Color(30, 30, 30, 150));

        userButton.addActionListener(e -> {
            isAdmin = false;
            cardLayout.show(mainPanel, "USER_LOGIN_REGISTER");
        });

        adminButton.addActionListener(e -> {
            String password = JOptionPane.showInputDialog("Enter admin password:");
            if (password != null && password.equals(ADMIN_PASSWORD)) {
                isAdmin = true;
                cardLayout.show(mainPanel, "ADMIN");
            } else {
                JOptionPane.showMessageDialog(null, "Invalid password!");
            }
        });

        buttonPanel.add(userButton);
        buttonPanel.add(adminButton);

        gbc.gridx = 0; gbc.gridy = 1; gbc.insets = new Insets(30, 0, 0, 0);
        loginPanel.add(buttonPanel, gbc);

        mainPanel.add(loginPanel, "LOGIN");
    }

    private JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hoverColor);
            }
            public void mouseExited(MouseEvent e) {
                button.setBackground(bgColor);
            }
        });

        return button;
    }

    private JPanel createStyledPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(20, 0, 30, 0);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(new Color(51, 51, 51));
        panel.add(titleLabel, gbc);
        return panel;
    }

    private void createUserPanel() {
        JPanel userPanel = createStyledPanel("User Dashboard");
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 15, 15));
        buttonPanel.setOpaque(false);
        JButton searchButton = createStyledButton("Search Rooms", new Color(70, 130, 180), new Color(100, 149, 237));
        JButton bookButton = createStyledButton("Book Room", new Color(34, 139, 34), new Color(60, 179, 113));
        JButton cancelButton = createStyledButton("Cancel Reservation", new Color(220, 20, 60), new Color(255, 69, 0));
        JButton backButton = createStyledButton("Logout", new Color(105, 105, 105), new Color(169, 169, 169));

        searchButton.addActionListener(e -> searchRooms());
        bookButton.addActionListener(e -> bookRoom());
        cancelButton.addActionListener(e -> cancelReservation());
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));

        buttonPanel.add(searchButton);
        buttonPanel.add(bookButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(backButton);
        gbc.gridx = 0; gbc.gridy = 1;
        userPanel.add(buttonPanel, gbc);
        mainPanel.add(userPanel, "USER");
    }

    private void createAdminPanel() {
        JPanel adminPanel = createStyledPanel("Admin Dashboard");
        GridBagConstraints gbc = new GridBagConstraints();
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 15, 15));
        buttonPanel.setOpaque(false);
        JButton addRoomButton = createStyledButton("Add Room", new Color(0, 128, 0), new Color(50, 205, 50));
        JButton deleteRoomButton = createStyledButton("Delete Room", new Color(178, 34, 34), new Color(220, 20, 60));
        JButton viewRoomsButton = createStyledButton("View All Rooms", new Color(65, 105, 225), new Color(100, 149, 237));
        JButton viewBookingsButton = createStyledButton("View All Bookings", new Color(255, 140, 0), new Color(255, 165, 0));
        JButton backButton = createStyledButton("Logout", new Color(105, 105, 105), new Color(169, 169, 169));

        addRoomButton.addActionListener(e -> addRoom());
        deleteRoomButton.addActionListener(e -> deleteRoom());
        viewRoomsButton.addActionListener(e -> viewAllRooms());
        viewBookingsButton.addActionListener(e -> viewAllBookings());
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));

        buttonPanel.add(addRoomButton);
        buttonPanel.add(deleteRoomButton);
        buttonPanel.add(viewRoomsButton);
        buttonPanel.add(viewBookingsButton);
        buttonPanel.add(backButton);

        gbc.gridx = 0; gbc.gridy = 1;
        adminPanel.add(buttonPanel, gbc);
        mainPanel.add(adminPanel, "ADMIN");
    }

    private void createLoginOrRegisterPanel() {
        JPanel loginRegisterPanel = new JPanel(new GridBagLayout());
        loginRegisterPanel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel loginLabel = new JLabel("User Login");
        loginLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginRegisterPanel.add(loginLabel, gbc);

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(15);
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(15);
        JButton loginButton = createStyledButton("Login", new Color(0, 102, 204), new Color(0, 122, 244));

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.gridx = 0; loginRegisterPanel.add(userLabel, gbc);
        gbc.gridx = 1; loginRegisterPanel.add(userField, gbc);
        gbc.gridy = 2; gbc.gridx = 0; loginRegisterPanel.add(passLabel, gbc);
        gbc.gridx = 1; loginRegisterPanel.add(passField, gbc);
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2; loginRegisterPanel.add(loginButton, gbc);

        JLabel registerLabel = new JLabel("New User Registration");
        registerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridy = 4; gbc.insets = new Insets(30, 10, 10, 10);
        loginRegisterPanel.add(registerLabel, gbc);

        JLabel regUserLabel = new JLabel("Username:");
        JTextField regUserField = new JTextField(15);
        JLabel regPassLabel = new JLabel("Password:");
        JPasswordField regPassField = new JPasswordField(15);
        JLabel regMobileLabel = new JLabel("Mobile Number:");
        JTextField regMobileField = new JTextField(15);
        JLabel regEmailLabel = new JLabel("Email:");
        JTextField regEmailField = new JTextField(15);
        JButton registerButton = createStyledButton("Register", new Color(34, 139, 34), new Color(60, 179, 113));

        gbc.gridy = 5; gbc.insets = new Insets(10, 10, 10, 10); gbc.gridwidth = 1; gbc.gridx = 0; loginRegisterPanel.add(regUserLabel, gbc);
        gbc.gridx = 1; loginRegisterPanel.add(regUserField, gbc);
        gbc.gridy = 6; gbc.gridx = 0; loginRegisterPanel.add(regPassLabel, gbc);
        gbc.gridx = 1; loginRegisterPanel.add(regPassField, gbc);
        gbc.gridy = 7; gbc.gridx = 0; loginRegisterPanel.add(regMobileLabel, gbc);
        gbc.gridx = 1; loginRegisterPanel.add(regMobileField, gbc);
        gbc.gridy = 8; gbc.gridx = 0; loginRegisterPanel.add(regEmailLabel, gbc);
        gbc.gridx = 1; loginRegisterPanel.add(regEmailField, gbc);
        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 2; loginRegisterPanel.add(registerButton, gbc);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cardLayout.show(mainPanel, "LOGIN"));
        gbc.gridy = 10; gbc.gridx = 0; gbc.gridwidth = 2; loginRegisterPanel.add(backButton, gbc);

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            boolean loggedIn = users.stream().anyMatch(user -> user.getUsername().equals(username) && user.getPassword().equals(password));

            if (loggedIn) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                cardLayout.show(mainPanel, "USER");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.");
            }
        });

        registerButton.addActionListener(e -> {
            String username = regUserField.getText();
            String password = new String(regPassField.getPassword());
            String mobile = regMobileField.getText();
            String email = regEmailField.getText();

            if (username.isEmpty() || password.isEmpty() || mobile.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields to register.");
                return;
            }

            boolean userExists = users.stream().anyMatch(user -> user.getUsername().equals(username));
            if (userExists) {
                JOptionPane.showMessageDialog(this, "Username already exists. Please choose another.");
            } else {
                users.add(new User(username, password, mobile, email));
                saveData();
                JOptionPane.showMessageDialog(this, "Registration successful!");
                regUserField.setText("");
                regPassField.setText("");
                regMobileField.setText("");
                regEmailField.setText("");
            }
        });

        mainPanel.add(loginRegisterPanel, "USER_LOGIN_REGISTER");
    }

    private void searchRooms() {
        String[] categories = {"Standard", "Deluxe", "Suite"};
        String selectedCategory = (String) JOptionPane.showInputDialog(this,
                "Select room category:", "Search Rooms", JOptionPane.QUESTION_MESSAGE, null, categories, categories[0]);

        if (selectedCategory != null) {
            StringBuilder available = new StringBuilder("Available " + selectedCategory + " rooms:\n\n");
            boolean found = false;
            for (Room room : rooms) {
                if (room.getCategory().equals(selectedCategory) && room.isAvailable()) {
                    found = true;
                    available.append("Room ").append(room.getRoomNumber()).append(" - Price: $").append(room.getPrice()).append("\n");
                }
            }
            if (!found) {
                available.append("No available rooms in this category.");
            }
            JOptionPane.showMessageDialog(this, available.toString());
        }
    }

    private void bookRoom() {
        String[] categories = {"Standard", "Deluxe", "Suite"};
        String selectedCategory = (String) JOptionPane.showInputDialog(this,
                "Select room category:", "Book Room", JOptionPane.QUESTION_MESSAGE, null, categories, categories[0]);

        if (selectedCategory != null) {
            Room selectedRoom = rooms.stream().filter(room -> room.getCategory().equals(selectedCategory) && room.isAvailable()).findFirst().orElse(null);

            if (selectedRoom != null) {
                String guestName = JOptionPane.showInputDialog("Enter guest name:");
                if (guestName == null || guestName.trim().isEmpty()) {
                    return;
                }

                String checkIn = showDatePickerDialog("Select Check-in Date");
                if (checkIn == null) { return; }

                String checkOut = showDatePickerDialog("Select Check-out Date");
                if (checkOut == null) { return; }

                long numberOfNights = calculateNights(checkIn, checkOut);

                if (numberOfNights <= 0) {
                    JOptionPane.showMessageDialog(this, "Incorrect dates! Check-out date must be after the check-in date.");
                    return;
                }

                double totalAmount = selectedRoom.getPrice() * numberOfNights;
                String reservationId = generateRandomId();
                Reservation reservation = new Reservation(reservationId, selectedRoom.getRoomNumber(), guestName, checkIn, checkOut, totalAmount);
                reservations.add(reservation);
                selectedRoom.setAvailable(false);
                saveData();

                String confirmationMessage = String.format("Booking confirmed!\nReservation ID: %s\nRoom Number: %d\nNumber of Nights: %d\nTotal Amount: $%.2f",
                        reservationId, selectedRoom.getRoomNumber(), numberOfNights, totalAmount);
                JOptionPane.showMessageDialog(this, confirmationMessage);
            } else {
                JOptionPane.showMessageDialog(this, "No available rooms in this category.");
            }
        }
    }

    private long calculateNights(String checkInStr, String checkOutStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            LocalDate checkInDate = LocalDate.parse(checkInStr, formatter);
            LocalDate checkOutDate = LocalDate.parse(checkOutStr, formatter);
            if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
                return -1;
            }
            return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing dates: " + e.getMessage());
            return -1;
        }
    }

    private void cancelReservation() {
        String reservationIdStr = JOptionPane.showInputDialog("Enter reservation ID:");
        if (reservationIdStr != null && !reservationIdStr.trim().isEmpty()) {
            Reservation toRemove = null;
            for (Reservation reservation : reservations) {
                if (reservation.getReservationId().equalsIgnoreCase(reservationIdStr.trim())) {
                    toRemove = reservation;
                    break;
                }
            }
            if (toRemove != null) {
                for (Room room : rooms) {
                    if (room.getRoomNumber() == toRemove.getRoomNumber()) {
                        room.setAvailable(true);
                        break;
                    }
                }
                reservations.remove(toRemove);
                saveData();
                JOptionPane.showMessageDialog(this, "Reservation " + toRemove.getReservationId() + " cancelled successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Reservation not found!");
            }
        }
    }

    private void addRoom() {
        try {
            String roomNumberStr = JOptionPane.showInputDialog("Enter room number:");
            if (roomNumberStr != null) {
                int roomNumber = Integer.parseInt(roomNumberStr);
                String[] categories = {"Standard", "Deluxe", "Suite"};
                String category = (String) JOptionPane.showInputDialog(this,
                        "Select room category:", "Add Room", JOptionPane.QUESTION_MESSAGE, null, categories, categories[0]);
                String priceStr = JOptionPane.showInputDialog("Enter room price:");
                if (category != null && priceStr != null) {
                    double price = Double.parseDouble(priceStr);
                    rooms.add(new Room(roomNumber, category, price));
                    saveData();
                    JOptionPane.showMessageDialog(this, "Room added successfully!");
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input!");
        }
    }

    private void deleteRoom() {
        String roomNumberStr = JOptionPane.showInputDialog("Enter room number to delete:");
        if (roomNumberStr != null) {
            try {
                int roomNumber = Integer.parseInt(roomNumberStr);
                Room toRemove = rooms.stream().filter(room -> room.getRoomNumber() == roomNumber).findFirst().orElse(null);
                if (toRemove != null) {
                    rooms.remove(toRemove);
                    saveData();
                    JOptionPane.showMessageDialog(this, "Room deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Room not found!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid room number!");
            }
        }
    }

    private void viewAllRooms() {
        StringBuilder roomList = new StringBuilder("All Rooms:\n\n");
        rooms.forEach(room -> roomList.append("Room ").append(room.getRoomNumber()).append(" - ").append(room.getCategory())
                .append(" - $").append(room.getPrice()).append(" - ").append(room.isAvailable() ? "Available" : "Booked").append("\n"));
        JOptionPane.showMessageDialog(this, roomList.toString());
    }

    private void viewAllBookings() {
        StringBuilder bookingList = new StringBuilder("All Bookings:\n\n");
        if (reservations.isEmpty()) {
            bookingList.append("No bookings found.");
        } else {
            reservations.forEach(reservation -> bookingList.append("Reservation ID: ").append(reservation.getReservationId())
                    .append("\nGuest: ").append(reservation.getGuestName()).append("\nRoom: ").append(reservation.getRoomNumber()).append("\n\n"));
        }
        JOptionPane.showMessageDialog(this, bookingList.toString());
    }

    private void saveData() {
        try {
            try (ObjectOutputStream roomsOut = new ObjectOutputStream(new FileOutputStream("rooms.dat"));
                 ObjectOutputStream reservationsOut = new ObjectOutputStream(new FileOutputStream("reservations.dat"));
                 ObjectOutputStream usersOut = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {

                roomsOut.writeObject(rooms);
                reservationsOut.writeObject(reservations);
                usersOut.writeObject(users);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving data!");
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        try {
            try (ObjectInputStream roomsIn = new ObjectInputStream(new FileInputStream("rooms.dat"));
                 ObjectInputStream reservationsIn = new ObjectInputStream(new FileInputStream("reservations.dat"));
                 ObjectInputStream usersIn = new ObjectInputStream(new FileInputStream(USERS_FILE))) {

                rooms = (ArrayList<Room>) roomsIn.readObject();
                reservations = (ArrayList<Reservation>) reservationsIn.readObject();
                users = (ArrayList<User>) usersIn.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            rooms = new ArrayList<>();
            reservations = new ArrayList<>();
            users = new ArrayList<>();
        }
    }

    private String generateRandomId() {
        String uniqueID = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        if (reservations.stream().anyMatch(res -> res.getReservationId().equals(uniqueID))) {
            return generateRandomId();
        }
        return uniqueID;
    }

   private String showDatePickerDialog(String title) {
    JDateChooser dateChooser = new JDateChooser();
    dateChooser.setDateFormatString("dd/MM/yyyy");

    // Get the current date and set it as the minimum selectable date
    Calendar today = Calendar.getInstance();
    today.set(Calendar.HOUR_OF_DAY, 0);
    today.set(Calendar.MINUTE, 0);
    today.set(Calendar.SECOND, 0);
    today.set(Calendar.MILLISECOND, 0);
    dateChooser.setMinSelectableDate(today.getTime());

    // Set the initial date on the calendar to today
    dateChooser.setDate(today.getTime());

    JPanel panel = new JPanel(new GridLayout(2, 1));
    panel.add(new JLabel(title));
    panel.add(dateChooser);

    int result = JOptionPane.showConfirmDialog(
        this,
        panel,
        title,
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE
    );

    if (result == JOptionPane.OK_OPTION) {
        Date selectedDate = dateChooser.getDate();
        if (selectedDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(selectedDate);
        }
    }
    return null;
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HotelReservationSystem().setVisible(true);
        });
    }
}