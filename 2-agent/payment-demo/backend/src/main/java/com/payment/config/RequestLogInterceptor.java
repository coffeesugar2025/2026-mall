package com.payment.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Component
public class RequestLogInterceptor implements HandlerInterceptor {

    // 保存请求开始时间 key
    private static final String START_TIME = "request_start_time";

    /**
     * controller执行之前
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 跳过静态资源、非控制器请求
        String path = request.getRequestURI();
        if (path.startsWith("/static") || path.startsWith("/favicon.ico")) {
            return true;
        }
        // 记录开始时间
        request.setAttribute(START_TIME, System.currentTimeMillis());

        String ip = getRealIp(request);
        String method = request.getMethod();
        String url = request.getRequestURI();
        String query = request.getQueryString();

        log.info("【请求入参】IP:{}, method:{}, url:{}, query:{}", ip, method, url, query);
        return true;
    }

    /**
     * controller执行完毕，视图渲染之前
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // postHandle 拿不到响应体
    }

    /**
     * 整个请求处理完毕（视图渲染完成之后），异常也会进这里
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Long start = (Long) request.getAttribute(START_TIME);
        if(start == null){
            return;
        }
        long cost = System.currentTimeMillis() - start;
        int status = response.getStatus();
        String uri = request.getRequestURI();

        if(ex != null){
            log.error("【请求异常】URI:{}, status:{}, cost:{}ms, ex:{}", uri, status, cost, ex.getMessage());
        }else {
            log.info("【请求完成】URI:{}, status:{}, cost:{}ms", uri, status, cost);
        }
    }


    /**
     * 获取真实客户端IP
     */
    private String getRealIp(HttpServletRequest request){
        String xff = request.getHeader("X-Forwarded-For");
        if(xff != null && !xff.isBlank()){
            return xff.split(",")[0].trim();
        }
        String xri = request.getHeader("X-Real‑IP");
        if(xri != null && !xri.isBlank()){
            return xri;
        }
        return request.getRemoteAddr();
    }
}
