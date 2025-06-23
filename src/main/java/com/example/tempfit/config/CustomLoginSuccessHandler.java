package com.example.tempfit.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import com.example.tempfit.security.AuthMemberDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        AuthMemberDTO authMemberDTO = (AuthMemberDTO) authentication.getPrincipal();
        log.info("CustomLoginSuccessHandler {}", authMemberDTO);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.getSession().setAttribute(
        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
        SecurityContextHolder.getContext()
    );

        Set<String> roleSet = new HashSet();
        authMemberDTO.getAuthorities().forEach(auth -> {
            roleSet.add(auth.getAuthority());
        });
        log.info("roleSet {}", roleSet);

        if (roleSet.contains("ROLE_ADMIN")||roleSet.contains("ROLE_MANAGER")){
            response.sendRedirect("/");
            return;
        }
        if (roleSet.contains("ROLE_USER")){
            response.sendRedirect("/");
            return;
        }
        response.sendRedirect("/");
    }

}
