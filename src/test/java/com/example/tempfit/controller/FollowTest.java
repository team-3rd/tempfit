package com.example.tempfit.controller;

import com.example.tempfit.service.FollowService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FollowTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    FollowService followService;

   @Test
    @WithUserDetails(
            value = "test1@naver.com",
            userDetailsServiceBeanName = "memberDetailsService",
            setupBefore = TestExecutionEvent.TEST_EXECUTION // 🔹 변경
    )
    void followUserTest() throws Exception {
        mockMvc.perform(post("/follow/yeongd3@gmail.com")
                        .header("X-CSRF-TOKEN", "dummyToken")) // CSRF 토큰 처리 필요 시 수정
                .andExpect(status().isOk());
    }
}