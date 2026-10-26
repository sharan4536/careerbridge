package com.careerbridge;

/**
 * JavaFX Application Launcher.
 * 
 * Provides a standard static main method entry point that avoids JavaFX runtime
 * module loading restrictions when launching without VM module options.
 */
public class Launcher {
    public static void main(String[] args) {
        AdminDashboard.main(args);
    }
}

