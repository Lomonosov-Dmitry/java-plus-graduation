package ru.practicum.service;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentDto save(Long userId, Long eventId, NewCommentDto newCommentDto);

    List<CommentDto> getAll(Long eventId, int from, int size);

    CommentDto update(Long userId, Long eventId, Long commentId, NewCommentDto newCommentDto);

    void delete(Long userId, Long eventId, Long commentId);

    CommentDto adminUpdate(Long eventId, Long commentId, NewCommentDto newCommentDto);

    void adminDelete(Long eventId, Long commentId);

}
