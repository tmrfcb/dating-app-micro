package com.tmrfcb.datingapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A Facebook.
 */
@Entity
@Table(name = "facebook")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "facebook")
public class Facebook implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnoreProperties(value = { "facebook", "relations", "location" }, allowSetters = true)
    @OneToOne(mappedBy = "facebook")
    private UserApp userApp;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Facebook id(Long id) {
        this.id = id;
        return this;
    }

    public UserApp getUserApp() {
        return this.userApp;
    }

    public Facebook userApp(UserApp userApp) {
        this.setUserApp(userApp);
        return this;
    }

    public void setUserApp(UserApp userApp) {
        if (this.userApp != null) {
            this.userApp.setFacebook(null);
        }
        if (userApp != null) {
            userApp.setFacebook(this);
        }
        this.userApp = userApp;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Facebook)) {
            return false;
        }
        return id != null && id.equals(((Facebook) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Facebook{" +
            "id=" + getId() +
            "}";
    }
}
