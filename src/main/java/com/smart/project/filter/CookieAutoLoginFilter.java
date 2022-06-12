package com.smart.project.filter;

import com.smart.project.util.ClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Slf4j
public class CookieAutoLoginFilter implements HandlerInterceptor {

    // 무조건 로그인이 되어 있어야 접근 가능한 경로 중 예외 경로
    final static String[] COERCION_ACCESS_URLS = {
    };

    // 무조건 로그인이 되어 있어야 접근 가능한 경로
    final static String[] ONLY_LOGIN_ACCESS_URLS = {

    };

    private double preTime = 0.0;
    private double afterTime = 0.0;


    /*@Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        preTime = new Date().getTime();//시작시간
        log.error("preHandle===>{}", request.getRequestURI());
        if(!checkUrlPermission(request, response)){
            String protocol = request.isSecure() ? "https://" : "http://";
            response.sendRedirect(protocol + request.getServerName() + "/member/login/index");
            return false;
        }
        return true;
//        return super.preHandle(request, response, handler);
    }*/


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        preTime = new Date().getTime();//시작시간
        log.error("preHandle===>{}", request.getRequestURI());
        if(!checkUrlPermission(request, response)){
            String protocol = request.isSecure() ? "https://" : "http://";
            response.sendRedirect(protocol + request.getServerName() + "/member/login/index");
            return false;
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
    /**********************************************************************************************
     * @Method 설명 : 여보야 스토어 정기점검 체크 루틴
     * @작성일 : 2021-08-26
     * @작성자 : 김남현
     * @변경이력 :
     **********************************************************************************************/
    private boolean storeChkExtracted(HttpServletResponse response, String reqUrl) {
        //biz api 호출시 800 에러 할당
        if(reqUrl.indexOf("/biz") == 0){
            log.error("###biz###");
            response.setStatus(800);
            return true;
        }
        return false;
    }

    /**
     * 접속한 URL의 접근 권한을 확인한다.
     *
     * @param request
     * @param response
     * @return
     */
    private boolean checkUrlPermission(HttpServletRequest request, HttpServletResponse response) {
        try {
            boolean isLogin = checkLogin(request);
            String accessUrl = request.getRequestURI();

            //* 접근 제한 링크
            if(!checkNoLoginAccess(accessUrl)) {
                //로그인 전용페이지
                log.info("This session is logout status... : " + request.getRemoteAddr() + " : No Accessable Url : "+accessUrl);
                return false;
            }
            return true;
        }catch(Exception e){
            log.info("Login checkPermission Exception...."+ e.getMessage());
            return false;
        }
    }

    /**
     * cookie에 로그인 사용자 정보가 존재하는 지와 유효한지 확인한다.
     *
     * @param request
     * @return
     */
    private boolean checkLogin(HttpServletRequest request){
        try {
            Map<String, String> cookieMap = ClientUtil.getCurrentCookie(request);
            String USER_ID = cookieMap.get("USER_ID");

            return (StringUtils.isNotEmpty(USER_ID));
        }catch(Exception e){
            log.info("checkLogin Exception...."+ e.getMessage());
            return false;
        }
    }

    /**
     * 로그인 전 접속 불가 페이지 확인
     *
     * @param accessUrl 접속URL
     * @return
     * @throws Exception
     */
    private boolean checkNoLoginAccess(String accessUrl) throws Exception{
        // 로그인이 되어있지 않아도 강제적으로 허용 해준다.
        List<String> list = Arrays.asList(COERCION_ACCESS_URLS);
        for(String url : list){
            if(accessUrl.startsWith(url))
                return true;
        }

        list = Arrays.asList(ONLY_LOGIN_ACCESS_URLS);
        for(String url : list){
//            log.error("{}, {}", url, accessUrl);
            if(accessUrl.startsWith(url))
                return false;
        }
        return true;
    }

    @Override
    public void afterCompletion (HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        afterTime = new Date().getTime();
        double delayTime = afterTime - preTime;
        //3초보다 서버처리속도 느릴경우출력
        if(delayTime > 3000){
            String requestURI = request.getRequestURI();
            log.warn("tiem : {} / URI : {} ", delayTime, requestURI);
        }
//        super.afterCompletion(request, response, handler, ex);
    }

    /*@Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        setNoCacheControl(response);
        log.error("postHandle===>{}", request.getRequestURI());
        super.postHandle(request, response, handler, modelAndView);
    }*/



    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        setNoCacheControl(response);
        log.error("postHandle===>{}", request.getRequestURI());
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    /**
     * spring security 에서 기본으로 처리되는 cache control 부분을 추가함. (kbs123)
     *
     * @param response
     */
    private void setNoCacheControl(HttpServletResponse response){
        response.setHeader("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
    }

    private boolean checkNotNull(String value){
        return !checkNull(value);
    }

    private boolean checkNull(String value){
        return (value == null || value.equals(""));
    }


}
