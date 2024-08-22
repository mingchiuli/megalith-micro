package org.chiu.micro.search.utils;

import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2023-03-30 9:20 pm
 */
public class ESHighlightBuilderUtils {
    private ESHighlightBuilderUtils() {}

    private static final String TITLE = "title";

    private static final String DESCRIPTION = "description";

    private static final String CONTENT = "content";

    private static final String RED = "<b style='color:red'>";

    private static final String BLABEL = "</b>";


    public static final HighlightQuery blogHighlightQueryOrigin = new HighlightQuery(
            new Highlight(
                    new HighlightParameters
                            .HighlightParametersBuilder()
                            .withPreTags(RED)
                            .withPostTags(BLABEL)
                            .build(),
                    List.of(new HighlightField(TITLE),
                            new HighlightField(DESCRIPTION),
                            new HighlightField(CONTENT))),
            null);

    public static final HighlightQuery blogHighlightQuerySimple = new HighlightQuery(
            new Highlight(
                    new HighlightParameters
                            .HighlightParametersBuilder()
                            .withPreTags(RED)
                            .withPostTags(BLABEL)
                            //为0则全部内容都显示
                            .withNumberOfFragments(1)
                            .withFragmentSize(5)
                            .build(),
                    List.of(new HighlightField(TITLE),
                            new HighlightField(DESCRIPTION),
                            new HighlightField(CONTENT))),
            null);

    public static final HighlightQuery websiteHighlightQuery = new HighlightQuery(
            new Highlight(
                    new HighlightParameters
                            .HighlightParametersBuilder()
                            .withPreTags(RED)
                            .withPostTags(BLABEL)
                            .build(),
                    List.of(new HighlightField(TITLE),
                            new HighlightField(DESCRIPTION))
            ), null);
}
