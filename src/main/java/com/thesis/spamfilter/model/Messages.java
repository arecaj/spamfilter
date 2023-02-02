package com.thesis.spamfilter.model;

import jakarta.persistence.*;
import org.springframework.data.relational.core.mapping.Table;

@Entity
@Table(name = "MESSAGES")
public class Messages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private int idMsg;
    private String message;
    private Boolean nbIsSpam;

    public Messages(){

    }

    public Messages(String message, Boolean nbIsSpam) {
        this.message = message;
        this.nbIsSpam = nbIsSpam;
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
