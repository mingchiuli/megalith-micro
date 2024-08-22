package org.chiu.micro.gateway.req;

import lombok.Data;

/**
 * @Author limingjiu
 * @Date 2024/6/28 11:41
 **/
@Data
public class ImgUploadReq {

    private String fileName;

    private byte[] data;

}
