package wiki.chiu.micro.search.application.port.in;

import wiki.chiu.micro.search.application.model.IndexRebuildResult;

public interface RebuildSearchIndex {

    IndexRebuildResult rebuild();
}
