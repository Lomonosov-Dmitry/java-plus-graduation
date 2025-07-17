package ru.practicum.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dal.CompilationRepository;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.mappers.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.service.CompilationService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    @Autowired
    CompilationRepository compilationRepository;

    @Override
    public CompilationDto create(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.INSTANCE.getCompilation(newCompilationDto);
        compilation = compilationRepository.save(compilation);

        return CompilationMapper.INSTANCE.getCompilationDto(compilation);
    }

    @Override
    public void deleteById(Long id) {
        if (!compilationRepository.existsById(id)) {
            throw new NotFoundException("compilation is not found with id = " + id);
        } else {
            compilationRepository.deleteById(id);
        }
    }

    @Override
    public CompilationDto getById(Long id) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("compilation is not found with id = " + id));

        return CompilationMapper.INSTANCE.getCompilationDto(compilation);
    }

    @Transactional
    @Override
    public CompilationDto updateById(Long id, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("compilation is not found with id = " + id));

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
}
