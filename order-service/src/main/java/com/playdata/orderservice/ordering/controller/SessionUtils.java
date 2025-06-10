package com.playdata.orderservice.ordering.controller;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

//카카오페이의 tid를 결제준비에서 결제승인으로 넘겨주기 위해 Session에 저장할 때 사용할 Util Class
@Slf4j
public class SessionUtils {


    public static void addAttribute(String name, Object value) {
        log.info("-------- session에 값 저장하기 위한 로직 [{}] ---", name);
        HttpSession session = getSession();
        log.info("session: {} ", session.getId());
        if (session != null) {
            session.setAttribute(name, value);
            log.info("Session ID: [{}] 에 저장된 주문 번호: {}", session.getId(), name); // 세션 ID와 함께 로그

            Object retrievedValue = session.getAttribute(name);
            if (retrievedValue != null) {
                log.info("Successfully added and retrieved '{}' with value: {}", name, retrievedValue);
            } else {
                log.warn("Failed to retrieve '{}' immediately after adding to session.", name);
            }

        } else {
            log.warn("Failed to get HttpSession in addAttribute for [{}]", name);
        }
    }

     public static String getStringAttributeValue(String name) {
        log.info("###--------- getStringAttributeValue: [{}] 값 조회를 위한 세션 가져오기 시도...", name);
        HttpSession session = getSession(); // getSession() 호출

        if (session != null) {
            log.info("### getStringAttributeValue: 세션을 성공적으로 가져왔습니다. 세션 ID: {}", session.getId());
            Object value = session.getAttribute(name);
            log.info("getStringAttributeValue: '{}' 조회 시도 - 현재 세션 ID: {}", name, session.getId());
            if (value != null) {
                log.info("getStringAttributeValue: '{}' 값을 세션에서 성공적으로 조회했습니다: {}", name, value);
            } else {
                log.warn("getStringAttributeValue: '{}' 값이 세션 ID {} 에 없습니다.", name, session.getId());
            }
            return (value != null) ? value.toString() : null;
        } else {

            log.warn("### getStringAttributeValue: HttpSession이 NULL입니다. [{}] 속성을 조회할 수 없습니다. 세션 컨텍스트 문제일 수 있습니다.", name);
        }
        log.info("### getStringAttributeValue: [{}] 처리 완료.", name);
        return null;
    }

    private static HttpSession getSession() {
        log.info("###-------- getSession: 현재 요청의 ServletRequestAttributes 가져오기 시도..."); // 이 로그가 찍히는지 중요!
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            if (attr == null) {
                log.error("### getSession: ServletRequestAttributes가 NULL입니다. 현재 스레드가 요청 컨텍스트에 바인딩되어 있지 않습니다.");
                return null;
            }

            // payReady의 경우, 세션이 없으면 새로 생성하도록 getSession(true) 사용
            HttpSession session = attr.getRequest().getSession(true);

            if (session == null) { // getSession(true)가 null을 반환하는 경우는 극히 드물지만 혹시 모를 상황 대비
                log.warn("### getSession: getSession(true) 호출 결과 HttpSession이 NULL입니다. 예상치 못한 상황.");
            } else {
                log.info("### getSession: HttpSession을 성공적으로 가져왔습니다. 세션 ID: {}", session.getId());
            }
            return session;
        } catch (IllegalStateException e) {
            log.error("### getSession: IllegalStateException 발생 - 요청 컨텍스트 밖에서 호출되었을 수 있습니다. {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("### getSession: 예상치 못한 예외 발생 - {}: {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return null;
        }
    }
}
