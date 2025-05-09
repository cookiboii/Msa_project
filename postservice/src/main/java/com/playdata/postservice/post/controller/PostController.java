package com.playdata.postservice.post.controller;

import com.playdata.postservice.common.auth.TokenUserInfo;
import com.playdata.postservice.common.dto.CommonErrorDto;
import com.playdata.postservice.common.dto.CommonResDto;
import com.playdata.postservice.post.dto.CommentResDto;
import com.playdata.postservice.post.dto.CommentSaveReqDto;
import com.playdata.postservice.post.dto.PostResDto;
import com.playdata.postservice.post.dto.PostSaveReqDto;
import com.playdata.postservice.post.entity.Comment;
import com.playdata.postservice.post.entity.Post;
import com.playdata.postservice.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
@Slf4j
public class PostController {

    private final PostService postService;

    // 질문 생성
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@AuthenticationPrincipal TokenUserInfo userInfo,
            @RequestBody PostSaveReqDto dto) {

        Post post = postService.createPost(dto, userInfo);

        PostResDto postDto = post.fromEntity();

        CommonResDto resDto = new CommonResDto(HttpStatus.CREATED, "질문 등록 성공", post.getId());

        return new ResponseEntity<>(resDto, HttpStatus.CREATED);
    }

    // 답변 생성
    @PostMapping("/comment/create")
    public ResponseEntity<?> createCommnet(@AuthenticationPrincipal TokenUserInfo userInfo,
                        @RequestBody CommentSaveReqDto dto) {

        Comment comment = postService.createComment(dto, userInfo);

        CommentResDto commentResDto = CommentResDto.builder()
                .content(comment.getContent())
                .postId(comment.getPost().getId())
                .userId(comment.getUserId())
                .build();

        CommonResDto resDto = new CommonResDto(HttpStatus.CREATED, "답변 등록 성공", comment.getId());

        return new ResponseEntity<>(resDto, HttpStatus.CREATED);
    }

    // 답변 삭제(회원일 때)
    @DeleteMapping("/comment/delete")
    public ResponseEntity<?> deleteComment(@AuthenticationPrincipal TokenUserInfo userInfo,
                                           @RequestParam("id") Long commentId){
        boolean isDeleted = postService.deleteCommentUser(commentId, userInfo);
        CommonResDto resDto = new CommonResDto();
        if(isDeleted){
            resDto.setResult(true);
            resDto.setStatusCode(HttpStatus.OK.value());
            resDto.setStatusMessage("댓글이 정상적으로 삭제됨. (회원용)");
            return new ResponseEntity<>(resDto, HttpStatus.OK);
        }
        else{

            resDto.setResult(false);
            resDto.setStatusCode(HttpStatus.BAD_REQUEST.value());
            resDto.setStatusMessage("댓글이 삭제되지 않음. 비정상적.id가 다른듯. (회원용)");
            return new ResponseEntity<>(resDto, HttpStatus.BAD_REQUEST);
        }

    }
    
    // 답변 삭제(관리자용)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/comment/deleteAdmin")
    public ResponseEntity<?> deleteCommentAdmin(@RequestParam("id") Long commentId){

        boolean isDeleted = postService.deleteCommentAdmin(commentId);
        CommonResDto resDto = new CommonResDto();
        if(isDeleted){
            resDto.setResult(true);
            resDto.setStatusCode(HttpStatus.OK.value());
            resDto.setStatusMessage("댓글이 정상적으로 삭제됨. (관리자용)");
            return new ResponseEntity<>(resDto, HttpStatus.OK);
        }
        else{

            resDto.setResult(false);
            resDto.setStatusCode(HttpStatus.BAD_REQUEST.value());
            resDto.setStatusMessage("댓글이 삭제되지 않음. 비정상적.id가 다른듯. (관리자용)");
            return new ResponseEntity<>(resDto, HttpStatus.BAD_REQUEST);
        }

    }

    // 마이페이지에서 본인의 질문 조회
    @GetMapping("/myquestions")
    public ResponseEntity<?> getMyQuestions(@AuthenticationPrincipal TokenUserInfo userInfo){

        List<PostResDto> postResDtoList = postService.getMyAllQuestions(userInfo);

        CommonResDto resDto = new CommonResDto(HttpStatus.OK, userInfo.getEmail()
                + "님의 질문 정보", postResDtoList);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }


    // 게시물 삭제 로직 (회원용)
    @DeleteMapping("/delete")
    public ResponseEntity<?> deletePostUser(@AuthenticationPrincipal TokenUserInfo userInfo,
                                            @RequestParam("id") Long postId){

        boolean isDeleted = postService.deletePostUser(userInfo, postId);
        if(!isDeleted){
            CommonErrorDto errorDto
                    = new CommonErrorDto(HttpStatus.UNAUTHORIZED, "해당 게시물의 작성자가 아닙니다.");
            return new ResponseEntity<>(errorDto, HttpStatus.UNAUTHORIZED);
        }
        CommonResDto resDto = new CommonResDto(HttpStatus.OK,
                "정상적으로 게시물이 삭제되었습니다.", postId);

        return new ResponseEntity<>(resDto, HttpStatus.OK);
    }

    // 게시물 삭제 (관리자용)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteadmin")
    public ResponseEntity<?> deletePostAdmin(@RequestParam("id") Long postId){
        postService.deletePostAdmin(postId);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    // 회원 탈퇴 시 해당 회원의 모든 게시물과 댓글을 삭제하는 로직
    @DeleteMapping("/deleteuser")
    public ResponseEntity<?> deletePostUser(@RequestParam("id") Long userId){

        postService.removeUserAllPost(userId);

        return new ResponseEntity<>(HttpStatus.OK);
    }


}
