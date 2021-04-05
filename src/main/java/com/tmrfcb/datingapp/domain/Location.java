package com.tmrfcb.datingapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A Location.
 */
@Entity
@Table(name = "location")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "location")
public class Location implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "street_address")
    private String streetAddress;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "city")
    private String city;

    @Column(name = "state_province")
    private String stateProvince;

    @Column(name = "gps")
    private Float gps;

    @OneToMany(mappedBy = "location")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "facebook", "relations", "location" }, allowSetters = true)
    private Set<UserApp> userApps = new HashSet<>();

    @JsonIgnoreProperties(value = { "location" }, allowSetters = true)
    @OneToOne(mappedBy = "location")
    private Country country;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Location id(Long id) {
        this.id = id;
        return this;
    }

    public String getStreetAddress() {
        return this.streetAddress;
    }

    public Location streetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
        return this;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPostalCode() {
        return this.postalCode;
    }

    public Location postalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return this.city;
    }

    public Location city(String city) {
        this.city = city;
        return this;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStateProvince() {
        return this.stateProvince;
    }

    public Location stateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
        return this;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public Float getGps() {
        return this.gps;
    }

    public Location gps(Float gps) {
        this.gps = gps;
        return this;
    }

    public void setGps(Float gps) {
        this.gps = gps;
    }

    public Set<UserApp> getUserApps() {
        return this.userApps;
    }

    public Location userApps(Set<UserApp> userApps) {
        this.setUserApps(userApps);
        return this;
    }

    public Location addUserApp(UserApp userApp) {
        this.userApps.add(userApp);
        userApp.setLocation(this);
        return this;
    }

    public Location removeUserApp(UserApp userApp) {
        this.userApps.remove(userApp);
        userApp.setLocation(null);
        return this;
    }

    public void setUserApps(Set<UserApp> userApps) {
        if (this.userApps != null) {
            this.userApps.forEach(i -> i.setLocation(null));
        }
        if (userApps != null) {
            userApps.forEach(i -> i.setLocation(this));
        }
        this.userApps = userApps;
    }

    public Country getCountry() {
        return this.country;
    }

    public Location country(Country country) {
        this.setCountry(country);
        return this;
    }

    public void setCountry(Country country) {
        if (this.country != null) {
            this.country.setLocation(null);
        }
        if (country != null) {
            country.setLocation(this);
        }
        this.country = country;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Location)) {
            return false;
        }
        return id != null && id.equals(((Location) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Location{" +
            "id=" + getId() +
            ", streetAddress='" + getStreetAddress() + "'" +
            ", postalCode='" + getPostalCode() + "'" +
            ", city='" + getCity() + "'" +
            ", stateProvince='" + getStateProvince() + "'" +
            ", gps=" + getGps() +
            "}";
    }
}
