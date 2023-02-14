package com.thesis.spamfilter.model;

import jakarta.persistence.*;
import org.springframework.data.relational.core.mapping.Table;

@Entity
@Table(name = "BLACKLIST")
public class Blacklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private int idBl;
    private String token;

    public Blacklist() {
    }

    public Blacklist(String word){
        this.token = word;
    }

    public void setId(int idBl) {
        this.idBl = idBl;
    }

    public int getId() {
        return idBl;
    }

    @Column(name = "token", nullable = false)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
