package com.perfectspring.topic04_webmvc.project;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * RECORDS ROOM layer. In-memory storage to keep this topic focused on the WEB part.
 * (Swap it for the JdbcTemplate version from Topic 3, or JPA from Topic 7, and nothing above it changes.)
 *
 * ConcurrentHashMap / AtomicLong: safe when many web requests arrive at the same time.
 */
@Repository
public class HomeworkRepository {

    private final Map<Long, Homework> storage = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public HomeworkRepository() {
        save(new Homework(null, "Maths", "Exercise 4.2, questions 1-10"));
        save(new Homework(null, "English", "Write an essay: My best holiday"));
    }

    public Homework save(Homework homework) {
        if (homework.getId() == null) {
            homework.setId(nextId.getAndIncrement());
        }
        storage.put(homework.getId(), homework);
        return homework;
    }

    public List<Homework> findAll() {
        return new ArrayList<>(storage.values());
    }

    public Optional<Homework> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public boolean deleteById(long id) {
        return storage.remove(id) != null;
    }
}
