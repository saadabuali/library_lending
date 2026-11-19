package edu.library.ui;

import edu.library.exception.StorageException;
import edu.library.persistence.LibraryStorage;
import edu.library.persistence.JsonFileStorage;
import edu.library.service.Library;

import java.nio.file.Path;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Path file = Path.of(args.length > 0 ? args[0] : "data/library.json");
        LibraryStorage storage = new JsonFileStorage(file);
        Library library;
        try {
            library = storage.load();
        } catch (StorageException e) {
            System.out.println("Could not load saved data: " + e.getMessage());
            System.out.println("Fix or delete the file and start again.");
            return;
        }
        new ConsoleUI(library, storage, new Scanner(System.in)).run();
    }
}
