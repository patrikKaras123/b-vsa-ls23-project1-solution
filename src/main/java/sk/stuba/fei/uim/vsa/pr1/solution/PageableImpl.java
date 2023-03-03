package sk.stuba.fei.uim.vsa.pr1.solution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sk.stuba.fei.uim.vsa.pr1.bonus.Pageable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageableImpl implements Pageable {

    private Integer pageNumber;
    private Integer pageSize;

    @Override
    public Pageable of(int page, int size) {
        return new PageableImpl(page, size);
    }

    @Override
    public Pageable first() {
        return new PageableImpl(0, this.pageSize);
    }

    @Override
    public Pageable previous() {
        if (pageNumber == 0) return this.first();
        return new PageableImpl(pageNumber - 1, pageSize);
    }

    @Override
    public Pageable next() {
        return new PageableImpl(pageNumber + 1, pageSize);
    }

    @Override
    public Integer getPageNumber() {
        return pageNumber;
    }

    @Override
    public Integer getPageSize() {
        return pageSize;
    }
}
