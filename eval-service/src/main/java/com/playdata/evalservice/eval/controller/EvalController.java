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


    // 평가 생성

    @PostMapping("/create")
    public ResponseEntity<?> createEvaluation(@AuthenticationPrincipal TokenUserInfo userInfo,
                                              @RequestBody EvalSaveReqDto reqDto){

        EvalResDto resDto = evalService.createEval(userInfo, reqDto);

        CommonResDto<EvalResDto> dto =
                new CommonResDto<>(HttpStatus.CREATED, "평가 생성됨!", resDto);



        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // 평가 삭제

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteEvaluation(@AuthenticationPrincipal TokenUserInfo userInfo,
                                              @RequestParam Long evalId){



    }

    // 마이페이지에서 평가 조회

    // 강의 디테일에 대한 평가 조회

    // 강의 리스트가 화면단에 출력될 때, 평점을 보여주기 위한 강의의 전체 평점 평균 조회

    //
}
