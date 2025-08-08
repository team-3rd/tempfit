package com.example.tempfit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatUserDTO {
    private String email;
    private String name;
    //private String profileImageUrl;
    
    public String getEmail() { return email; }
    public String getName() { return name; }
}