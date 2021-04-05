package com.tmrfcb.datingapp.domain;

import java.io.Serializable;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A ReportUser.
 */
@Entity
@Table(name = "report_user")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "reportuser")
public class ReportUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id")
    private String reportId;

    @Column(name = "user_id")
    private String userId;

    @Lob
    @Column(name = "cause")
    private String cause;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReportUser id(Long id) {
        this.id = id;
        return this;
    }

    public String getReportId() {
        return this.reportId;
    }

    public ReportUser reportId(String reportId) {
        this.reportId = reportId;
        return this;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getUserId() {
        return this.userId;
    }

    public ReportUser userId(String userId) {
        this.userId = userId;
        return this;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCause() {
        return this.cause;
    }

    public ReportUser cause(String cause) {
        this.cause = cause;
        return this;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReportUser)) {
            return false;
        }
        return id != null && id.equals(((ReportUser) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReportUser{" +
            "id=" + getId() +
            ", reportId='" + getReportId() + "'" +
            ", userId='" + getUserId() + "'" +
            ", cause='" + getCause() + "'" +
            "}";
    }
}
