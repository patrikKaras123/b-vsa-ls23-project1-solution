package sk.stuba.fei.uim.vsa.pr1.bonus;

public interface Pageable {

    Pageable first();

    Pageable last();

    Pageable previous();

    Pageable next();

    int getPageNumber();

    int getPageSize();

}
