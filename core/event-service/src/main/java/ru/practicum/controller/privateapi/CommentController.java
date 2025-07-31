package ru.practicum.controller.privateapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.service.CommentService;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/{userId}/events/{eventId}/comments")
    public CommentDto saveComment(@PathVariable Long userId,
                                  @PathVariable Long eventId,
                                  @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("saving comment for userId {} and eventId {}", userId, eventId);
        return commentService.save(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{userId}/events/{eventId}/comments/{commentId}")
    public CommentDto updateComment(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @PathVariable Long commentId,
                                    @RequestBody @Valid NewCommentDto newCommentDto) {
        log.info("updating comment for userId {} and eventId {}", userId, eventId);
        return commentService.update(userId, eventId, commentId, newCommentDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{userId}/events/{eventId}/comments/{commentId}")
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long eventId,
                              @PathVariable Long commentId) {
        log.info("deleting comment with id {} for userId {} and eventId {}", commentId, userId, eventId);
        commentService.delete(userId, eventId, commentId);
    }
}
