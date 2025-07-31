package ru.practicum.service.impl;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dal.CompilationRepository;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mappers.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.service.CompilationService;

import java.util.List;

@Slf4j
@Service
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;

    public CompilationServiceImpl(CompilationRepository compilationRepository) {
        this.compilationRepository = compilationRepository;
    }

    @Override
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.INSTANCE.getCompilation(newCompilationDto);
        compilation = compilationRepository.save(compilation);

        return CompilationMapper.INSTANCE.getCompilationDto(compilation);
    }

    @Override
    public void deleteById(Long id) {
        getCompilation(id);
        compilationRepository.deleteById(id);
    }

    @Override
    public CompilationDto getById(Long id) {
        return CompilationMapper.INSTANCE.getCompilationDto(getCompilation(id));
    }

    @Transactional
    @Override
    public CompilationDto updateById(Long id, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = getCompilation(id);

        CompilationMapper.INSTANCE.update(compilation, updateCompilationRequest);
        return CompilationMapper.INSTANCE.getCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> getCompilationsByFilter(Boolean pinned, int from, int size) {
        Pageable pageable = PageRequest.of(from, size);

        return compilationRepository.findAllByFilterPublic(pinned, pageable).stream()
                .map(CompilationMapper.INSTANCE::getCompilationDto)
                .toList();
    }

    private Compilation getCompilation(Long id) {
        return compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("compilation is not found with id = " + id));
    }
}
