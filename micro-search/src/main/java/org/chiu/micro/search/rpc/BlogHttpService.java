package org.chiu.micro.search.rpc;

import org.chiu.micro.search.dto.BlogEntityDto;
import org.chiu.micro.search.lang.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface BlogHttpService {

    @GetExchange("/blog/{blogId}")
    Result<BlogEntityDto> findById(@PathVariable Long blogId);
}
