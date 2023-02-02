package com.thesis.spamfilter.model;

import jakarta.persistence.*;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Entity
@Table(name = "BLACKLIST")
public class Blacklist {
    @Id
    private int idBl;
    private String word;

    public Blacklist() {
    }

    public Blacklist(String word){
        this.word = word;
    }

    public void setId(int idBl) {
        this.idBl = idBl;
    }

    public int getId() {
        return idBl;
    }

    @Column(name = "word", nullable = false)
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
