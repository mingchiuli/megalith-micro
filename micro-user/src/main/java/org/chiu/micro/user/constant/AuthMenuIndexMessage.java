package org.chiu.micro.user.constant;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthMenuIndexMessage {
    
    private List<String> roles;

    private Integer type;
}
