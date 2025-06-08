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
         log.info("--- session에 값 저장하기 위한 로직 [{}] ---", name);
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(name, value);
            log.info("Session ID where attribute [{}] is added: {}", session.getId(), name); // 세션 ID와 함께 로그

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
        HttpSession session = getSession();
        if (session != null) {
            Object value = session.getAttribute(name);
            return (value != null) ? value.toString() : null;
        }
        return null;
    }

    private static HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        // true: 세션이 없으면 새로 생성, false: 없으면 null 반환
        // 여기서는 이미 세션이 존재한다고 가정하고 false로 할 수도 있습니다.
        // payReady에서 세션이 생성되므로, payCompleted에서는 기존 세션을 얻어야 합니다.
        return attr.getRequest().getSession(false); // 기존 세션 가져오기 (없으면 생성 안 함)
    }
}
