package com.example.tempfit.dto;

import java.time.LocalDateTime;

import lombok.*;

@Data
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageForm {
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime sentAt;
}
