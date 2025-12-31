package com.jandi.plan_backend.commu.comment.service;

import com.jandi.plan_backend.commu.comment.dto.CommentReportRespDTO;
import com.jandi.plan_backend.commu.comment.entity.Comment;
import com.jandi.plan_backend.commu.comment.entity.CommentReported;
import com.jandi.plan_backend.commu.comment.repository.CommentReportedRepository;
import com.jandi.plan_backend.commu.community.dto.ReportReqDTO;
import com.jandi.plan_backend.user.entity.User;
import com.jandi.plan_backend.util.TimeUtil;
import com.jandi.plan_backend.util.ValidationUtil;
import com.jandi.plan_backend.util.service.BadRequestExceptionMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentReportService {
    private final ValidationUtil validationUtil;
    private final CommentReportedRepository commentReportedRepository;

    /** 댓글 신고 */
    @Transactional
    public CommentReportRespDTO reportComment(String userEmail, Integer commentId, ReportReqDTO reportDTO) {
        User user = validationUtil.validateUserExists(userEmail);
        validationUtil.validateUserRestricted(user);
        Comment comment = validationUtil.validateCommentExists(commentId);

        if (commentReportedRepository.findByUser_userIdAndComment_CommentId(user.getUserId(), commentId).isPresent()) {
            throw new BadRequestExceptionMessage("이미 신고한 댓글입니다");
        }

        CommentReported commentReported = new CommentReported();
        commentReported.setComment(comment);
        commentReported.setUser(user);
        commentReported.setContents(reportDTO.getContents());
        commentReported.setCreatedAt(TimeUtil.now());
        commentReportedRepository.save(commentReported);
        return new CommentReportRespDTO(commentReported);
    }
}
