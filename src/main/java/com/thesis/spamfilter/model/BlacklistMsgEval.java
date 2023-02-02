package com.thesis.spamfilter.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "BLACKLIST_MSG_EVAL")
public class BlacklistMsgEval {
    @Id
    private int idBlMsg;
    private int idBl;
    private int idMsg;
    private boolean isSpam;

    public BlacklistMsgEval(int idBl, int idMsg, boolean isSpam) {
        this.idBl = idBl;
        this.idMsg = idMsg;
        this.isSpam = isSpam;
    }

    public BlacklistMsgEval() {

    }

    public int getIdBlMsg() {
        return idBlMsg;
    }

    public void setIdBlMsg(int idBlMsg) {
        this.idBlMsg = idBlMsg;
    }

    public int getIdBl() {
        return idBl;
    }

    public void setIdBl(int idBl) {
        this.idBl = idBl;
    }

    public int getIdMsg() {
        return idMsg;
    }

    public void setIdMsg(int idMsg) {
        this.idMsg = idMsg;
    }

    public boolean getIsSpam() {
        return isSpam;
    }

    public void setIsSpam(boolean isSpam) {
        this.isSpam = isSpam;
    }
}
