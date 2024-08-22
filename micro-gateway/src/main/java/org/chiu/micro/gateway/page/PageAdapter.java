package org.chiu.micro.gateway.page;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author mingchiuli
 * @create 2022-11-29 12:58 am
 */
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PageAdapter<T> {

    private List<T> content;

    private long totalElements;

    private int pageNumber;

    private int pageSize;

    private boolean first;

    private boolean last;

    private boolean empty;

    private int totalPages;

}
