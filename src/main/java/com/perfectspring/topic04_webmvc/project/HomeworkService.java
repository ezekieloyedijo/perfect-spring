package com.perfectspring.topic04_webmvc.project;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * TEACHERS layer: the business rules. Knows NOTHING about HTTP.
 * (So it could also be used by a console app, a scheduled job, a test...)
 */
@Service
public class HomeworkService {

    private final HomeworkRepository repository;

    public HomeworkService(HomeworkRepository repository) {
        this.repository = repository;
    }

    public List<Homework> findAll(String subjectFilter) {
        return repository.findAll().stream()
                .filter(h -> subjectFilter == null || h.getSubject().equalsIgnoreCase(subjectFilter))
                .sorted(Comparator.comparing(Homework::getId))
                .toList();
    }

    public Homework findOne(long id) {
        return repository.findById(id).orElseThrow(() -> new HomeworkNotFoundException(id));
    }

    public Homework create(Homework homework) {
        // A business rule: every homework must have a subject.
        if (homework.getSubject() == null || homework.getSubject().isBlank()) {
            throw new IllegalArgumentException("subject must not be empty");
        }
        homework.setId(null);       // the server chooses the id, not the client
        homework.setDone(false);
        return repository.save(homework);
    }

    public Homework markDone(long id) {
        Homework homework = findOne(id);
        homework.setDone(true);
        return repository.save(homework);
    }

    public void delete(long id) {
        if (!repository.deleteById(id)) {
            throw new HomeworkNotFoundException(id);
        }
    }
}
