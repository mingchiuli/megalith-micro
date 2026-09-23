package wiki.chiu.micro.search.config;

import org.springframework.aot.hint.BindingReflectionHintsRegistrar;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.result.Result;
import wiki.chiu.micro.search.adapter.in.http.BlogDocumentVo;
import wiki.chiu.micro.search.api.req.BlogReadCountReq;
import wiki.chiu.micro.search.api.req.BlogSysCountSearchReq;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.IndexRebuildRpcVo;

public class SearchRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.resources().registerPattern("script/*.painless");

        // Payload types bound by the functional routes and the index source client.
        new BindingReflectionHintsRegistrar()
            .registerReflectionHints(
                hints.reflection(),
                BlogSnapshot.class,
                BlogIndexSourceStatus.class,
                Result.class,
                PageAdapter.class,
                BlogSysSearchReq.class,
                BlogSysCountSearchReq.class,
                BlogReadCountReq.class,
                BlogDocumentVo.class,
                IndexRebuildRpcVo.class);
    }
}
