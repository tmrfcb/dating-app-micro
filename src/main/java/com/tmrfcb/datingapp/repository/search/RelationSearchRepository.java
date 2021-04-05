package com.tmrfcb.datingapp.repository.search;

import com.tmrfcb.datingapp.domain.Relation;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Relation} entity.
 */
public interface RelationSearchRepository extends ElasticsearchRepository<Relation, Long> {}
