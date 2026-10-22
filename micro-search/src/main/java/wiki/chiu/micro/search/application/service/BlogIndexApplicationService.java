package wiki.chiu.micro.search.application.service;

import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.port.in.ApplyBlogIndexChangeUseCase;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;

public final class BlogIndexApplicationService implements ApplyBlogIndexChangeUseCase {

    private final BlogIndexWriter indexWriter;

    public BlogIndexApplicationService(BlogIndexWriter indexWriter) {
        this.indexWriter = indexWriter;
    }

    @Override
    public void apply(BlogIndexChange change) {
        indexWriter.write(change);
    }

    @Override
    public void applyAdmin(BlogAdminIndexChange change) {
        indexWriter.writeAdmin(change);
    }
}
