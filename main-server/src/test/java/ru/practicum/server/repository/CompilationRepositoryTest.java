package ru.practicum.server.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.server.model.compilation.Compilation;
import ru.practicum.server.repository.compilation.CompilationRepository;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CompilationRepositoryTest {

    @Autowired
    private CompilationRepository compilationRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_ShouldPersistCompilation() {
        Compilation compilation = Compilation.builder()
                .title("Летние события")
                .pinned(true)
                .events(new HashSet<>())
                .build();

        Compilation saved = compilationRepository.save(compilation);

        assertNotNull(saved.getId());
        assertEquals("Летние события", saved.getTitle());
        assertTrue(saved.getPinned());
    }

    @Test
    void findByPinned_ShouldReturnCompilations() {
        Compilation comp1 = Compilation.builder()
                .title("Летние события")
                .pinned(true)
                .events(new HashSet<>())
                .build();
        Compilation comp2 = Compilation.builder()
                .title("Зимние события")
                .pinned(false)
                .events(new HashSet<>())
                .build();

        entityManager.persist(comp1);
        entityManager.persist(comp2);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);
        var compilations = compilationRepository.findByPinned(true, pageable);

        assertEquals(1, compilations.size());
        assertTrue(compilations.get(0).getPinned());
    }
}