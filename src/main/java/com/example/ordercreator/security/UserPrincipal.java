package com.example.ordercreator.security;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class UserPrincipal {
    private String userId;
    private String email;
    private List<String> roles;
}
