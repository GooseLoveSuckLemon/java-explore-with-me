package ru.practicum.explore.repository.compilation;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explore.model.compilation.Compilation;

import java.util.List;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    List<Compilation> findByPinned(Boolean pinned, Pageable pageable);
}
