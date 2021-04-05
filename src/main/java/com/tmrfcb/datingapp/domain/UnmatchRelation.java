package com.tmrfcb.datingapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A UnmatchRelation.
 */
@Entity
@Table(name = "unmatch_relation")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "unmatchrelation")
public class UnmatchRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "un_match_date")
    private ZonedDateTime unMatchDate;

    @JsonIgnoreProperties(value = { "matchRelation", "unmatchRelation", "userApp" }, allowSetters = true)
    @OneToOne(mappedBy = "unmatchRelation")
    private Relation relation;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UnmatchRelation id(Long id) {
        this.id = id;
        return this;
    }

    public ZonedDateTime getUnMatchDate() {
        return this.unMatchDate;
    }

    public UnmatchRelation unMatchDate(ZonedDateTime unMatchDate) {
        this.unMatchDate = unMatchDate;
        return this;
    }

    public void setUnMatchDate(ZonedDateTime unMatchDate) {
        this.unMatchDate = unMatchDate;
    }

    public Relation getRelation() {
        return this.relation;
    }

    public UnmatchRelation relation(Relation relation) {
        this.setRelation(relation);
        return this;
    }

    public void setRelation(Relation relation) {
        if (this.relation != null) {
            this.relation.setUnmatchRelation(null);
        }
        if (relation != null) {
            relation.setUnmatchRelation(this);
        }
        this.relation = relation;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UnmatchRelation)) {
            return false;
        }
        return id != null && id.equals(((UnmatchRelation) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UnmatchRelation{" +
            "id=" + getId() +
            ", unMatchDate='" + getUnMatchDate() + "'" +
            "}";
    }
}
