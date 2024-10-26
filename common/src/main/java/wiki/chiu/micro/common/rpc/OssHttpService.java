package wiki.chiu.micro.common.rpc;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.Map;

public interface OssHttpService {

    @PutExchange("/{objectName}")
    void putOssObject(@PathVariable String objectName, @RequestBody byte[] body, @RequestHeader Map<String, String> headers);

    @DeleteExchange("/{objectName}")
    void deleteOssObject(@PathVariable String objectName,  @RequestHeader Map<String, String> headers);
}
