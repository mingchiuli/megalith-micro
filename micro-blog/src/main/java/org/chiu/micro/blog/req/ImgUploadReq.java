package org.chiu.micro.blog.req;

import lombok.Data;

@Data
public class ImgUploadReq {

    private byte[] data;

    private String fileName;
}
