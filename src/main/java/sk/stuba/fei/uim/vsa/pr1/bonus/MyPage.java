package sk.stuba.fei.uim.vsa.pr1.bonus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MyPage<R> implements Page<R>{

    private List<R> content;
    private Pageable pageable;

    private long totalElements;

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
        this.totalElements = totalElements;
    }

    @Override
    public int getTotalPages() {
        return (int) Math.ceil((double)this.totalElements / this.pageable.getPageSize());
    }
}