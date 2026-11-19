package edu.library.persistence;

import edu.library.exception.StorageException;
import edu.library.service.Library;

/** Abstraction over where library data lives (file today, database tomorrow). */
public interface LibraryStorage {
    void save(Library library) throws StorageException;

    Library load() throws StorageException;
}
