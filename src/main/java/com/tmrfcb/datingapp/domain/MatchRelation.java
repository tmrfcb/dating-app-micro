package com.tmrfcb.datingapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A MatchRelation.
 */
@Entity
@Table(name = "match_relation")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "matchrelation")
public class MatchRelation implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "match_date")
    private ZonedDateTime matchDate;

    @OneToMany(mappedBy = "matchRelation")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "matchRelation" }, allowSetters = true)
    private Set<Message> messages = new HashSet<>();

    @JsonIgnoreProperties(value = { "matchRelation", "unmatchRelation", "userApp" }, allowSetters = true)
    @OneToOne(mappedBy = "matchRelation")
    private Relation relation;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MatchRelation id(Long id) {
        this.id = id;
        return this;
    }

    public ZonedDateTime getMatchDate() {
        return this.matchDate;
    }

    public MatchRelation matchDate(ZonedDateTime matchDate) {
        this.matchDate = matchDate;
        return this;
    }

    public void setMatchDate(ZonedDateTime matchDate) {
        this.matchDate = matchDate;
    }

    public Set<Message> getMessages() {
        return this.messages;
    }

    public MatchRelation messages(Set<Message> messages) {
        this.setMessages(messages);
        return this;
    }

    public MatchRelation addMessage(Message message) {
        this.messages.add(message);
        message.setMatchRelation(this);
        return this;
    }

    public MatchRelation removeMessage(Message message) {
        this.messages.remove(message);
        message.setMatchRelation(null);
        return this;
    }

    public void setMessages(Set<Message> messages) {
        if (this.messages != null) {
            this.messages.forEach(i -> i.setMatchRelation(null));
        }
        if (messages != null) {
            messages.forEach(i -> i.setMatchRelation(this));
        }
        this.messages = messages;
    }

    public Relation getRelation() {
        return this.relation;
    }

    public MatchRelation relation(Relation relation) {
        this.setRelation(relation);
        return this;
    }

    public void setRelation(Relation relation) {
        if (this.relation != null) {
            this.relation.setMatchRelation(null);
        }
        if (relation != null) {
            relation.setMatchRelation(this);
        }
        this.relation = relation;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MatchRelation)) {
            return false;
        }
        return id != null && id.equals(((MatchRelation) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MatchRelation{" +
            "id=" + getId() +
            ", matchDate='" + getMatchDate() + "'" +
            "}";
    }
}
