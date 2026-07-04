package ru.practicum.explore.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.explore.dto.compilation.CompilationDto;
import ru.practicum.explore.dto.compilation.NewCompilationDto;
import ru.practicum.explore.dto.compilation.UpdateCompilationRequest;
import ru.practicum.explore.exception.NotFoundException;
import ru.practicum.explore.model.category.Category;
import ru.practicum.explore.model.compilation.Compilation;
import ru.practicum.explore.model.event.Event;
import ru.practicum.explore.repository.compilation.CompilationRepository;
import ru.practicum.explore.repository.event.EventRepository;
import ru.practicum.explore.service.compilation.CompilationServiceImpl;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompilationServiceTest {

    @Mock
    private CompilationRepository compilationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private StatsIntegrationService statsIntegrationService;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    @Test
    void createCompilation_ShouldReturnCompilationDto() {
        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("Летние события");
        dto.setPinned(true);
        dto.setEvents(List.of(1L, 2L));

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        Event event1 = Event.builder()
                .id(1L)
                .annotation("Событие 1")
                .category(category)
                .build();
        Event event2 = Event.builder()
                .id(2L)
                .annotation("Событие 2")
                .category(category)
                .build();

        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Летние события")
                .pinned(true)
                .events(new HashSet<>(Set.of(event1, event2)))
                .build();

        when(eventRepository.findAllById(anyList())).thenReturn(List.of(event1, event2));
        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);

        CompilationDto result = compilationService.createCompilation(dto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Летние события", result.getTitle());
        assertTrue(result.getPinned());
        assertEquals(2, result.getEvents().size());

        verify(eventRepository).findAllById(List.of(1L, 2L));
        verify(compilationRepository).save(any(Compilation.class));
    }

    @Test
    void createCompilation_WithEmptyEvents_ShouldReturnCompilationDto() {
        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("Летние события");
        dto.setPinned(true);
        dto.setEvents(null);

        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Летние события")
                .pinned(true)
                .events(new HashSet<>())
                .build();

        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);

        CompilationDto result = compilationService.createCompilation(dto);

        assertNotNull(result);
        assertEquals("Летние события", result.getTitle());
        assertTrue(result.getPinned());
        assertTrue(result.getEvents().isEmpty());

        verify(compilationRepository).save(any(Compilation.class));
    }

    @Test
    void deleteCompilation_ShouldDeleteCompilation() {
        when(compilationRepository.existsById(1L)).thenReturn(true);

        compilationService.deleteCompilation(1L);

        verify(compilationRepository).existsById(1L);
        verify(compilationRepository).deleteById(1L);
    }

    @Test
    void deleteCompilation_WithInvalidId_ShouldThrowNotFoundException() {
        when(compilationRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> compilationService.deleteCompilation(1L));

        verify(compilationRepository).existsById(1L);
        verify(compilationRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateCompilation_ShouldReturnUpdatedCompilation() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("Зимние события");
        request.setPinned(false);
        request.setEvents(List.of(1L));

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        Event event = Event.builder()
                .id(1L)
                .annotation("Событие")
                .category(category)
                .build();

        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Летние события")
                .pinned(true)
                .events(new HashSet<>())
                .build();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(eventRepository.findAllById(anyList())).thenReturn(List.of(event));
        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);

        CompilationDto result = compilationService.updateCompilation(1L, request);

        assertNotNull(result);
        assertEquals("Зимние события", result.getTitle());
        assertFalse(result.getPinned());

        verify(compilationRepository).findById(1L);
        verify(compilationRepository).save(any(Compilation.class));
    }

    @Test
    void updateCompilation_WithInvalidId_ShouldThrowNotFoundException() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("Зимние события");

        when(compilationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> compilationService.updateCompilation(1L, request));

        verify(compilationRepository).findById(1L);
        verify(compilationRepository, never()).save(any(Compilation.class));
    }

    @Test
    void updateCompilation_WithNullFields_ShouldNotUpdateThem() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("Зимние события");

        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        Event event = Event.builder()
                .id(1L)
                .annotation("Событие")
                .category(category)
                .build();

        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Летние события")
                .pinned(true)
                .events(new HashSet<>(Set.of(event)))
                .build();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));
        when(compilationRepository.save(any(Compilation.class))).thenReturn(compilation);

        CompilationDto result = compilationService.updateCompilation(1L, request);

        assertNotNull(result);
        assertEquals("Зимние события", result.getTitle());
        assertTrue(result.getPinned());

        verify(compilationRepository).findById(1L);
        verify(compilationRepository).save(any(Compilation.class));
    }

    @Test
    void getCompilations_ShouldReturnList() {
        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        Event event = Event.builder()
                .id(1L)
                .annotation("Событие")
                .category(category)
                .build();

        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Летние события")
                .pinned(true)
                .events(new HashSet<>(Set.of(event)))
                .build();

        Page<Compilation> page = new PageImpl<>(List.of(compilation));

        when(compilationRepository.findAll(any(Pageable.class))).thenReturn(page);

        List<CompilationDto> result = compilationService.getCompilations(null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Летние события", result.get(0).getTitle());

        verify(compilationRepository).findAll(any(Pageable.class));
    }

    @Test
    void getCompilations_WithPinnedFilter_ShouldReturnFilteredList() {
        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        Event event = Event.builder()
                .id(1L)
                .annotation("Событие")
                .category(category)
                .build();

        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Летние события")
                .pinned(true)
                .events(new HashSet<>(Set.of(event)))
                .build();

        when(compilationRepository.findByPinned(eq(true), any(Pageable.class)))
                .thenReturn(List.of(compilation));

        List<CompilationDto> result = compilationService.getCompilations(true, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getPinned());

        verify(compilationRepository).findByPinned(eq(true), any(Pageable.class));
    }

    @Test
    void getCompilation_ShouldReturnCompilation() {
        Category category = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        Event event = Event.builder()
                .id(1L)
                .annotation("Событие")
                .category(category)
                .build();

        Compilation compilation = Compilation.builder()
                .id(1L)
                .title("Летние события")
                .pinned(true)
                .events(new HashSet<>(Set.of(event)))
                .build();

        when(compilationRepository.findById(1L)).thenReturn(Optional.of(compilation));

        CompilationDto result = compilationService.getCompilation(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Летние события", result.getTitle());
        assertTrue(result.getPinned());

        verify(compilationRepository).findById(1L);
    }

    @Test
    void getCompilation_WithInvalidId_ShouldThrowNotFoundException() {
        when(compilationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> compilationService.getCompilation(1L));

        verify(compilationRepository).findById(1L);
    }
}