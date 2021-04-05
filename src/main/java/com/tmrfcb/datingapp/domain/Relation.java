package com.tmrfcb.datingapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.tmrfcb.datingapp.domain.enumeration.RelationType;
import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A Relation.
 */
@Entity
@Table(name = "relation")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "relation")
public class Relation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id_of_other")
    private String userIdOfOther;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type")
    private RelationType relationType;

    @JsonIgnoreProperties(value = { "messages", "relation" }, allowSetters = true)
    @OneToOne
    @JoinColumn(unique = true)
    private MatchRelation matchRelation;

    @JsonIgnoreProperties(value = { "relation" }, allowSetters = true)
    @OneToOne
    @JoinColumn(unique = true)
    private UnmatchRelation unmatchRelation;

    @ManyToOne
    @JsonIgnoreProperties(value = { "facebook", "relations", "location" }, allowSetters = true)
    private UserApp userApp;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Relation id(Long id) {
        this.id = id;
        return this;
    }

    public String getUserIdOfOther() {
        return this.userIdOfOther;
    }

    public Relation userIdOfOther(String userIdOfOther) {
        this.userIdOfOther = userIdOfOther;
        return this;
    }

    public void setUserIdOfOther(String userIdOfOther) {
        this.userIdOfOther = userIdOfOther;
    }

    public RelationType getRelationType() {
        return this.relationType;
    }

    public Relation relationType(RelationType relationType) {
        this.relationType = relationType;
        return this;
    }

    public void setRelationType(RelationType relationType) {
        this.relationType = relationType;
    }

    public MatchRelation getMatchRelation() {
        return this.matchRelation;
    }

    public Relation matchRelation(MatchRelation matchRelation) {
        this.setMatchRelation(matchRelation);
        return this;
    }

    public void setMatchRelation(MatchRelation matchRelation) {
        this.matchRelation = matchRelation;
    }

    public UnmatchRelation getUnmatchRelation() {
        return this.unmatchRelation;
    }

    public Relation unmatchRelation(UnmatchRelation unmatchRelation) {
        this.setUnmatchRelation(unmatchRelation);
        return this;
    }

    public void setUnmatchRelation(UnmatchRelation unmatchRelation) {
        this.unmatchRelation = unmatchRelation;
    }

    public UserApp getUserApp() {
        return this.userApp;
    }

    public Relation userApp(UserApp userApp) {
        this.setUserApp(userApp);
        return this;
    }

    public void setUserApp(UserApp userApp) {
        this.userApp = userApp;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Relation)) {
            return false;
        }
        return id != null && id.equals(((Relation) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Relation{" +
            "id=" + getId() +
            ", userIdOfOther='" + getUserIdOfOther() + "'" +
            ", relationType='" + getRelationType() + "'" +
            "}";
    }
}
