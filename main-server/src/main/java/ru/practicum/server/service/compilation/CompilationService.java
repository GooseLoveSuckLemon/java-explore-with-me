package ru.practicum.server.service.compilation;

import ru.practicum.server.dto.compilation.CompilationDto;
import ru.practicum.server.dto.compilation.NewCompilationDto;
import ru.practicum.server.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    CompilationDto createCompilation(NewCompilationDto dto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request);

    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size);

    CompilationDto getCompilation(Long compId);
}