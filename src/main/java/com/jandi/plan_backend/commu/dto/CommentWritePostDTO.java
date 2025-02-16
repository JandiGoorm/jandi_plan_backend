package com.jandi.plan_backend.commu.dto;

import com.jandi.plan_backend.commu.entity.Comments;
import com.jandi.plan_backend.commu.entity.Community;
import lombok.Data;

/**
 * 댓글 작성 시 클라이언트로부터 전달되는 데이터를 담는 DTO
 * 댓글 관계(parentCommentId)와 댓글 내용을 저장한다.
 */
@Data
public class CommentWritePostDTO {
    private Integer postId;
    private Integer parentCommentId;
    private String contents;
}
