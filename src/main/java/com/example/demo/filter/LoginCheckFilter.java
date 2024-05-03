package com.example.demo.filter;

import com.alibaba.fastjson.JSON;
import com.example.demo.common.R;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")

public class LoginCheckFilter implements Filter{
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request =(HttpServletRequest) servletRequest;
        HttpServletResponse response =(HttpServletResponse) servletResponse;

        String requestURI=request.getRequestURI();

        String[]urls=new String[]{
                "/backend/**",
                "/login",
        };

        boolean check=check(urls,requestURI);
        if (check) {
            log.info("本次请求：{}，不需要处理",requestURI);
            filterChain.doFilter(request,response);
            return;
        }

        if (request.getSession().getAttribute("username") != null) {
            log.info("用户已登录，用户名为：{}",request.getSession().getAttribute("username"));

            filterChain.doFilter(request,response);
            return;
        }

//        filterChain.doFilter(request,response);
        //response.setHeader("location","/backend/page/login/login.html");
        log.info("用户未登录");
        //跳转回登陆页面
        // 设置响应状态码为302，表示临时重定向
        response.setStatus(HttpServletResponse.SC_FOUND);
        // 设置重定向的位置，这里假设你的登录页面的URL是 "/login.html"
        response.setHeader("Location", "/backend/page/login/login.html");

        return;

    }
    public boolean check(String[] urls, String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match) {
                //匹配
                return true;
            }
        }
        //不匹配
        return false;
    }


}
