package com.playdata.evalservice.eval.controller;

import com.playdata.evalservice.common.auth.TokenUserInfo;
import com.playdata.evalservice.common.dto.CommonResDto;
import com.playdata.evalservice.eval.dto.EvalResDto;
import com.playdata.evalservice.eval.dto.EvalSaveReqDto;
import com.playdata.evalservice.eval.entity.Eval;
import com.playdata.evalservice.eval.service.EvalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/eval")
@RequiredArgsConstructor
@Slf4j
public class EvalController {

    private final EvalService evalService;

    // 평가 생성 가능 여부 확인 로직
    @GetMapping("/cancreate/{id}")
    public ResponseEntity<?> canEval(@AuthenticationPrincipal TokenUserInfo userInfo, @PathVariable(name = "id") Long evalId) {
        boolean canCreate = evalService.canEvalCourse(userInfo, evalId);

        if(canCreate) {
            return new ResponseEntity<>(canCreate, HttpStatus.ACCEPTED);
        }
        else{
            return new ResponseEntity<>(canCreate, HttpStatus.FORBIDDEN);
        }
    }


    // 평가 생성

    @PostMapping("/create")
    public ResponseEntity<?> createEvaluation(@AuthenticationPrincipal TokenUserInfo userInfo,
                                              @RequestBody EvalSaveReqDto reqDto){

        EvalResDto resDto = evalService.createEval(userInfo, reqDto);
        
        // 이미 평가를 진행한 경우
        if(resDto == null) {
            return new ResponseEntity<>(HttpStatus.LOCKED);
        }
        
        CommonResDto<EvalResDto> dto =
                new CommonResDto<>(HttpStatus.CREATED, "평가 생성됨!", resDto);
        
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // 평가 삭제
    // 마이페이지, 강의 상세 정보에서 나오는 나의 평가에서만 삭제버튼이 존재할 것이기 때문에,
    // 딱히, 확인 로직을 강하게 하지는 않겠음.
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteEvaluation(@AuthenticationPrincipal TokenUserInfo userInfo,
                                              @PathVariable(name = "id") Long evalId){

        boolean isDeleted = evalService.deleteEval(userInfo, evalId);

        if(isDeleted){
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }
    
    // 평가 수정

    // 내 평가 조회
    
    // 마이페이지에서 평가 조회

    // 강의 디테일에 대한 평가 조회

    // 강의 리스트가 화면단에 출력될 때, 평점을 보여주기 위한 강의의 전체 평점 평균 조회

    //
}
