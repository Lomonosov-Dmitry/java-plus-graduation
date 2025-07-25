package ru.practicum.service;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto create(NewCompilationDto newCompilationDto);

    void deleteById(Long id);

    CompilationDto getById(Long id);

    CompilationDto updateById(Long id, UpdateCompilationRequest updateCompilationRequest);

    List<CompilationDto> getCompilationsByFilter(Boolean pinned, int from, int size);
}
