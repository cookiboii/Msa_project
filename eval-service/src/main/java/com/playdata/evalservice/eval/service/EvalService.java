package com.playdata.evalservice.eval.service;

import com.playdata.evalservice.client.CourseServiceClient;
import com.playdata.evalservice.client.UserServiceClient;
import com.playdata.evalservice.common.auth.TokenUserInfo;
import com.playdata.evalservice.common.dto.CommonResDto;
import com.playdata.evalservice.eval.dto.EvalResDto;
import com.playdata.evalservice.eval.dto.EvalSaveReqDto;
import com.playdata.evalservice.eval.dto.UserResDto;
import com.playdata.evalservice.eval.entity.Eval;
import com.playdata.evalservice.eval.repository.EvalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvalService {

    private final EvalRepository evalRepository;
    private final UserServiceClient userServiceClient;
    private final CourseServiceClient courseServiceClient;


    public EvalResDto createEval(TokenUserInfo userInfo, EvalSaveReqDto reqDto) {

        Long userId = getUserId(userInfo);

        Optional<Eval> foundEval
                = evalRepository.findByProductIdAndUserId(reqDto.getProductId(), userId);

        // 이미 해당 유저가 평가를 진행했다면
        if(foundEval.isPresent()) {
            return null;
        }

        Eval newEval = reqDto.toEntity(userId);
        Eval saved = evalRepository.save(newEval);

        return saved.fromEntity();
    }




    private Long getUserId(TokenUserInfo userInfo) {
        String email = userInfo.getEmail();

        CommonResDto<UserResDto> foundUser = userServiceClient.getIdByEmail(email);
        UserResDto result = foundUser.getResult();
        Long id = result.getId();

        log.info(result.toString());
        return id;

    }
}
