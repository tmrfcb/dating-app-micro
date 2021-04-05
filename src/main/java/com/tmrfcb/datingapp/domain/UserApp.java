package com.tmrfcb.datingapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A UserApp.
 */
@Entity
@Table(name = "user_app")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "userapp")
public class UserApp implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @JsonIgnoreProperties(value = { "userApp" }, allowSetters = true)
    @OneToOne
    @JoinColumn(unique = true)
    private Facebook facebook;

    @OneToMany(mappedBy = "userApp")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "matchRelation", "unmatchRelation", "userApp" }, allowSetters = true)
    private Set<Relation> relations = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = { "userApps", "country" }, allowSetters = true)
    private Location location;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserApp id(Long id) {
        this.id = id;
        return this;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public UserApp firstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public UserApp lastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return this.email;
    }

    public UserApp email(String email) {
        this.email = email;
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public UserApp phoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getBirthDate() {
        return this.birthDate;
    }

    public UserApp birthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
        return this;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Facebook getFacebook() {
        return this.facebook;
    }

    public UserApp facebook(Facebook facebook) {
        this.setFacebook(facebook);
        return this;
    }

    public void setFacebook(Facebook facebook) {
        this.facebook = facebook;
    }

    public Set<Relation> getRelations() {
        return this.relations;
    }

    public UserApp relations(Set<Relation> relations) {
        this.setRelations(relations);
        return this;
    }

    public UserApp addRelation(Relation relation) {
        this.relations.add(relation);
        relation.setUserApp(this);
        return this;
    }

    public UserApp removeRelation(Relation relation) {
        this.relations.remove(relation);
        relation.setUserApp(null);
        return this;
    }

    public void setRelations(Set<Relation> relations) {
        if (this.relations != null) {
            this.relations.forEach(i -> i.setUserApp(null));
        }
        if (relations != null) {
            relations.forEach(i -> i.setUserApp(this));
        }
        this.relations = relations;
    }

    public Location getLocation() {
        return this.location;
    }

    public UserApp location(Location location) {
        this.setLocation(location);
        return this;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserApp)) {
            return false;
        }
        return id != null && id.equals(((UserApp) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserApp{" +
            "id=" + getId() +
            ", firstName='" + getFirstName() + "'" +
            ", lastName='" + getLastName() + "'" +
            ", email='" + getEmail() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            ", birthDate='" + getBirthDate() + "'" +
            "}";
    }
}
