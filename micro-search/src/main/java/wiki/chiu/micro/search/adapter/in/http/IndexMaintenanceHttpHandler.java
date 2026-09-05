package wiki.chiu.micro.search.adapter.in.http;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.lang.CommonErrorCode;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.search.api.SearchIndexMaintenanceHttpService;
import wiki.chiu.micro.search.api.vo.IndexRebuildRpcVo;
import wiki.chiu.micro.search.application.model.IndexRebuildRejectedException;
import wiki.chiu.micro.search.application.port.in.RebuildSearchIndex;

@Component
public class IndexMaintenanceHttpHandler implements SearchIndexMaintenanceHttpService {

    private final RebuildSearchIndex indexes;
    private final Counter failures;
    private final Counter processed;
    private final Timer duration;
    private final AtomicLong lastSuccess;

    public IndexMaintenanceHttpHandler(RebuildSearchIndex indexes, MeterRegistry registry) {
        this.indexes = indexes;
        failures = registry.counter("megalith.search.rebuild.failures");
        processed = registry.counter("megalith.search.rebuild.documents");
        duration = registry.timer("megalith.search.rebuild.duration");
        lastSuccess = registry.gauge("megalith.search.rebuild.last.success", new AtomicLong());
    }

    public ServerResponse rebuild(ServerRequest request) {
        return ok(rebuild());
    }

    @Override
    public Result<IndexRebuildRpcVo> rebuild() {
        long started = System.nanoTime();
        try {
            var result = indexes.rebuild();
            processed.increment(result.documents());
            lastSuccess.set(Instant.now().getEpochSecond());
            return Result.success(new IndexRebuildRpcVo(result.previousIndex(), result.index(), result.documents()));
        } catch (IndexRebuildRejectedException rejected) {
            failures.increment();
            throw new BaseException(CommonErrorCode.CONFLICT, rejected.getMessage());
        } catch (RuntimeException failure) {
            failures.increment();
            throw failure;
        } finally {
            duration.record(System.nanoTime() - started, TimeUnit.NANOSECONDS);
        }
    }
}
