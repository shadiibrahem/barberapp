package com.barber.barberapp.model;

import jakarta.persistence.*;

@Entity
public class ShopSettings {

    @Id
    private Long id = 1L; // always 1 row

    @Column(length = 500)
    private String mapsLink;

    @Column(length = 200)
    private String instagramLink;

    // getters/setters
    public Long getId() { return id; }
    public String getMapsLink() { return mapsLink; }
    public void setMapsLink(String mapsLink) { this.mapsLink = mapsLink; }
    public String getInstagramLink() { return instagramLink; }
    public void setInstagramLink(String instagramLink) { this.instagramLink = instagramLink; }
}
