package org.chiu.micro.user.constant;

import java.io.Serializable;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthMenuIndexMessage implements Serializable {
    
    private List<String> roles;

    private Integer type;
}
