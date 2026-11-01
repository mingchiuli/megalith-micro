package wiki.chiu.micro.search.application.port.in;

import wiki.chiu.micro.search.application.model.SearchIndexRebuildResult;

public interface RebuildSearchIndex {

    SearchIndexRebuildResult rebuild();
}
