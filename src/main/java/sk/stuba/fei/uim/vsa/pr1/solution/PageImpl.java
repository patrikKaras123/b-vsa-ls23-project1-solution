package sk.stuba.fei.uim.vsa.pr1.solution;

import lombok.Data;
import sk.stuba.fei.uim.vsa.pr1.bonus.Page;
import sk.stuba.fei.uim.vsa.pr1.bonus.Pageable;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageImpl<R> implements Page<R> {

    private List<R> content;
    private Pageable pageable;
    private Long totalElements;
    private Integer totalPages;

    public PageImpl() {
        this.content = new ArrayList<>();
        this.totalElements = 0L;
        this.totalPages = 0;
    }

    public PageImpl(List<R> content, Pageable pageable) {
        this();
        this.content = content;
        this.pageable = pageable;
    }

    @Override
    public List<R> getContent() {
        return content;
    }

    @Override
    public Pageable getPageable() {
        return pageable;
    }

    @Override
    public Long getTotalElements() {
        return totalElements;
    }

    @Override
    public void setTotalElements(Long totalElements) {
        if (this.totalElements == null || this.totalElements == 0L)
            this.totalElements = totalElements;
    }

    @Override
    public int getTotalPages() {
        if ((totalPages == null || totalPages == 0) && this.totalElements != null) {
            totalPages = ((Double) Math.ceil(getTotalElements().doubleValue() / pageable.getPageSize().doubleValue())).intValue();
        }
        return totalPages;
    }

    @Override
    public String toString() {
        return "PageImpl{" +
                "pageable=" + pageable +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                '}';
    }
}
