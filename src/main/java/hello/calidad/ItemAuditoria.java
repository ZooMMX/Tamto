package hello.calidad;

/**
 * Proyecto tamto
 * User: octavioruizcastillo
 * Date: 08/05/15
 * Time: 14:20
 */
public class ItemAuditoria {
    public ItemAuditoria(String quien, String que, String cuando) {
        this.quien = quien;
        this.cuando = cuando;
        this.que = que;
    }

    private String cuando;
    private String que;
    private String quien;

    public String getCuando() {
        return cuando;
    }

    public void setCuando(String cuando) {
        this.cuando = cuando;
    }

    public String getQue() {
        return que;
    }

    public void setQue(String que) {
        this.que = que;
    }

    public String getQuien() {
        return quien;
    }

    public void setQuien(String quien) {
        this.quien = quien;
    }
}
