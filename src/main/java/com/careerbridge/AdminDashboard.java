package com.careerbridge;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


public class AdminDashboard extends Application {

    private static final String DB_URL = "jdbc:h2:file:./careerbridge_db;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CareerBridge Admin Dashboard - JDBC Persistence Manager");

        Label titleLabel = new Label("CareerBridge Registered Users (JDBC)");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TableView<UserRow> table = new TableView<>();

        TableColumn<UserRow, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        
        TableColumn<UserRow, String> emailCol = new TableColumn<>("Email Address");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(260);

        TableColumn<UserRow, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(140);

        table.getColumns().addAll(idCol, emailCol, roleCol);
        table.setPrefHeight(250);

        Label statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #4b5563;");

        Button refreshBtn = new Button("Refresh Data");
        refreshBtn.setOnAction(e -> {
            table.setItems(fetchUsers());
            statusLabel.setText("Data refreshed from database via JDBC.");
        });

        Button backupBtn = new Button("Save Database (JDBC Script)");
        backupBtn.setStyle("-fx-background-color: #2563eb; -fx-text-fill: white; -fx-font-weight: bold;");
        backupBtn.setOnAction(e -> {
            boolean success = saveDatabaseViaJdbc();
            if (success) {
                statusLabel.setText("Database saved successfully to ./backups/javafx_db_backup.sql via JDBC!");
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Database checkpointed and saved to ./backups/javafx_db_backup.sql");
                alert.setTitle("JDBC Persistence");
                alert.setHeaderText("Database Saved Successfully");
                alert.showAndWait();
            } else {
                statusLabel.setText("Error saving database backup via JDBC.");
            }
        });

        // Insert new user form via JDBC PreparedStatement
        Label insertLabel = new Label("Save New User to Database (JDBC INSERT):");
        insertLabel.setStyle("-fx-font-weight: bold;");

        TextField emailField = new TextField();
        emailField.setPromptText("user@careerbridge.com");
        emailField.setPrefWidth(180);

        TextField passField = new TextField();
        passField.setPromptText("password");
        passField.setPrefWidth(120);

        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("STUDENT", "RECRUITER", "ADMIN");
        roleCombo.setValue("STUDENT");

        Button saveUserBtn = new Button("Save User (JDBC)");
        saveUserBtn.setStyle("-fx-background-color: #16a34a; -fx-text-fill: white;");
        saveUserBtn.setOnAction(e -> {
            String email = emailField.getText();
            String pass = passField.getText();
            String role = roleCombo.getValue();
            if (email != null && !email.trim().isEmpty() && pass != null && !pass.trim().isEmpty()) {
                if (insertUserViaJdbc(email.trim(), pass.trim(), role)) {
                    emailField.clear();
                    passField.clear();
                    table.setItems(fetchUsers());
                    statusLabel.setText("User " + email + " successfully saved to database via JDBC PreparedStatement.");
                } else {
                    statusLabel.setText("Failed to save user. Check if email already exists.");
                }
            } else {
                statusLabel.setText("Please enter both email and password.");
            }
        });

        HBox formBox = new HBox(10, emailField, passField, roleCombo, saveUserBtn);
        HBox btnBox = new HBox(10, refreshBtn, backupBtn);

        VBox vbox = new VBox(12);
        vbox.setPadding(new Insets(20));
        vbox.getChildren().addAll(titleLabel, btnBox, table, insertLabel, formBox, statusLabel);

        Scene scene = new Scene(vbox, 680, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Initial fetch
        table.setItems(fetchUsers());
    }

    private ObservableList<UserRow> fetchUsers() {
        ObservableList<UserRow> users = FXCollections.observableArrayList();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, email, role FROM users")) {

            while (rs.next()) {
                users.add(new UserRow(rs.getLong("id"), rs.getString("email"), rs.getString("role")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Could not load users. Make sure the Spring Boot app has initialized the DB schema.");
        }
        return users;
    }

    private boolean insertUserViaJdbc(String email, String password, String role) {
        String sql = "INSERT INTO users (email, password, role, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, "{noop}" + password); // Simple password storage for direct JDBC insert
            pstmt.setString(3, role);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean saveDatabaseViaJdbc() {
        try {
            File backupDir = new File("./backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 Statement stmt = conn.createStatement()) {
                stmt.execute("CHECKPOINT SYNC");
                stmt.execute("SCRIPT TO './backups/javafx_db_backup.sql'");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // JavaFX JavaBean representation
    public static class UserRow {
        private Long id;
        private String email;
        private String role;

        public UserRow(Long id, String email, String role) {
            this.id = id;
            this.email = email;
            this.role = role;
        }

        public Long getId() { return id; }
        public String getEmail() { return email; }
        public String getRole() { return role; }
    }
}
