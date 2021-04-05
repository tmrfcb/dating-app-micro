package com.tmrfcb.datingapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tmrfcb.datingapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class UnmatchRelationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(UnmatchRelation.class);
        UnmatchRelation unmatchRelation1 = new UnmatchRelation();
        unmatchRelation1.setId(1L);
        UnmatchRelation unmatchRelation2 = new UnmatchRelation();
        unmatchRelation2.setId(unmatchRelation1.getId());
        assertThat(unmatchRelation1).isEqualTo(unmatchRelation2);
        unmatchRelation2.setId(2L);
        assertThat(unmatchRelation1).isNotEqualTo(unmatchRelation2);
        unmatchRelation1.setId(null);
        assertThat(unmatchRelation1).isNotEqualTo(unmatchRelation2);
    }
}
