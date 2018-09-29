package entity;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable{
    private Long totalPage;
    private List rowMessage;

    public PageResult(Long totalPage, List rowMessage) {
        this.totalPage = totalPage;
        this.rowMessage = rowMessage;
    }

    public Long getTotalPage() {

        return totalPage;
    }

    public void setTotalPage(Long totalPage) {
        this.totalPage = totalPage;
    }

    public List getRowMessage() {
        return rowMessage;
    }

    public void setRowMessage(List rowMessage) {
        this.rowMessage = rowMessage;
    }
}
