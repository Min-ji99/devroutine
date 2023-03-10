package com.likelion.devroutine.comment.service;

import com.likelion.devroutine.alarm.domain.Alarm;
import com.likelion.devroutine.alarm.enumurate.AlarmType;
import com.likelion.devroutine.alarm.repository.AlarmRepository;
import com.likelion.devroutine.certification.domain.Certification;
import com.likelion.devroutine.certification.exception.CertificationNotFoundException;
import com.likelion.devroutine.certification.repository.CertificationRepository;
import com.likelion.devroutine.comment.domain.Comment;
import com.likelion.devroutine.comment.dto.*;
import com.likelion.devroutine.comment.enumerate.ResponseMessage;
import com.likelion.devroutine.comment.exception.CommentNotFoundException;
import com.likelion.devroutine.comment.repository.CommentRepository;
import com.likelion.devroutine.user.domain.User;
import com.likelion.devroutine.user.exception.UserNotFoundException;
import com.likelion.devroutine.user.exception.UserUnauthorizedException;
import com.likelion.devroutine.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final CertificationRepository certificationRepository;

    private final AlarmRepository alarmRepository;

    @Transactional
    public CommentCreateResponse createComment(Long certificationId, CommentRequest request, String oauthId) {
        Certification certification = findCertification(certificationId);
        User user = findUserByOauthId(oauthId);
        Comment savedComment = commentRepository.save(Comment.createComment(request.getComment(), certification, user));
        saveCommentAlarm(certification.getId(), savedComment.getId(), user.getId());
        return CommentCreateResponse.of(savedComment);
    }

    public List<CommentResponse> findAll(Long certificationId, Pageable pageable) {
        validateCertificationExists(certificationId);
        return CommentResponse.of(commentRepository.findAllByCertificationId(certificationId, pageable));
    }

    @Transactional
    public CommentDeleteResponse deleteComment(Long certificationId, Long commentId, String oauthId) {
        validateCertificationExists(certificationId);
        Comment comment = getCommentByAuthorizedUser(commentId, oauthId);
        alarmRepository.deleteByTargetIdAndFromUserId(comment.getId(), findUserByOauthId(oauthId).getId());
        comment.deleteComment();
        return CommentDeleteResponse.of(ResponseMessage.COMMENT_DELETE_SUCCESS.getMessage(), commentId);
    }

    @Transactional
    public CommentUpdateResponse updateComment(Long certificationId, Long commentId, CommentRequest request, String oauthId) {
        validateCertificationExists(certificationId);
        Comment comment = getCommentByAuthorizedUser(commentId, oauthId);
        comment.updateComment(request.getComment());
        return CommentUpdateResponse.of(comment);
    }

    private Certification findCertification(Long certificationId) {
        return certificationRepository.findById(certificationId)
                .orElseThrow(CertificationNotFoundException::new);
    }

    private User findUserByOauthId(String OauthId) {
        return userRepository.findByOauthId(OauthId)
                .orElseThrow(UserNotFoundException::new);
    }

    private void validateCertificationExists(Long certificationId) {
        if (!certificationRepository.existsById(certificationId)) {
            throw new CertificationNotFoundException();
        }
    }

    private Comment getCommentByAuthorizedUser(Long commentId, String oauthId) {
        User findUser = findUserByOauthId(oauthId);
        Comment comment = getComment(commentId);
        if (comment.getUser().getId().equals(findUser.getId()))
            return comment;
        else throw new UserUnauthorizedException();
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);
    }

    public void saveCommentAlarm(Long certificationId, Long commentId, Long fromUserId) {
        User user = commentRepository.findUserByCommentParam(certificationId);
        if(fromUserId.equals(user.getId())) return;
        alarmRepository.save(Alarm.createAlarm(commentId,
                AlarmType.NEW_COMMENT_ON_CERTIFICATION,AlarmType.NEW_COMMENT_ON_CERTIFICATION.getMessage(), fromUserId, user));
    }

}

