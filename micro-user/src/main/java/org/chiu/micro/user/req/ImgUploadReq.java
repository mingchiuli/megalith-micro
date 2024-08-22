package org.chiu.micro.user.req;

import lombok.Data;

@Data
public class ImgUploadReq {

    private byte[] data;

    private String fileName;
}
