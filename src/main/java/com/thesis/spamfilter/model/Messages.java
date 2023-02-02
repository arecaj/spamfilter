package com.thesis.spamfilter.model;

import jakarta.persistence.*;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Entity
@Table(name = "MESSAGES")
public class Messages {
    @Id
    private int idMsg;
    private String message;
    private Boolean nbIsSpam;

    public Messages(){

    }

    public Messages(String message) {
        this.message = message;
    }

    @Column(name = "message", nullable = false)
    public String getMessage() {
        return message;
    }
    public void setMessage(String message, Boolean nbIsSpam) {
        this.message = message;
        this.nbIsSpam = nbIsSpam;
    }

    public int getId() {
        return idMsg;
    }

    public void setId(int idMsg) {
        this.idMsg = idMsg;
    }

    public Boolean getNbIsSpam() {
        return nbIsSpam;
    }

    public void setNbIsSpam(Boolean nbIsSpam) {
        this.nbIsSpam = nbIsSpam;
    }
}
