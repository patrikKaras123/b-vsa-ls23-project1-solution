package sk.stuba.fei.uim.vsa.pr1.bonus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class MyPageable implements Pageable {

    private int page;
    private int size;

    @Override
    public Pageable of(int page, int size) {
        return new MyPageable(page, size);
    }

    @Override
    public Pageable first() {
        return new MyPageable(0, size);
    }

    @Override
    public Pageable previous() {
        return new MyPageable(page - 1, size);
    }

    @Override
    public Pageable next() {

        return new MyPageable(page + 1, size);
    }

    @Override
    public Integer getPageNumber() {
        return page;
    }

    @Override
    public Integer getPageSize() {
        return size;
    }
}