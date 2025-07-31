package ru.practicum.controller.publicapi;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.service.CompilationService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
public class PublicCompilationController {
    @Autowired
    CompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(name = "from", defaultValue = "0") int from,
                                                @Positive @RequestParam(name = "size", defaultValue = "10") int size) {
        log.info("Getting all compilations from {} size {}", from, size);
        return compilationService.getCompilationsByFilter(pinned, from, size);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        log.info("Getting compilation with id {}", compId);

        return compilationService.getById(compId);
    }


}
