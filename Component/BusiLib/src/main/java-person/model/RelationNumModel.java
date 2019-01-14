package model;

import java.io.Serializable;

public class RelationNumModel implements Serializable {
    /**
     * relation : 1
     * cnt : 4
     */

    private int relation;
    private int cnt;

    public int getRelation() {
        return relation;
    }

    public void setRelation(int relation) {
        this.relation = relation;
    }

    public int getCnt() {
        return cnt;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }

}
