package org.chiu.micro.auth.constant;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Data;

/**
 * UserAuthMenuOperateMessage
 */
@Data
@Builder
public class UserAuthMenuOperateMessage implements Serializable {

	private List<String> roles;

	private Integer type;
}