package com.tmrfcb.datingapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tmrfcb.datingapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class MatchRelationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MatchRelation.class);
        MatchRelation matchRelation1 = new MatchRelation();
        matchRelation1.setId(1L);
        MatchRelation matchRelation2 = new MatchRelation();
        matchRelation2.setId(matchRelation1.getId());
        assertThat(matchRelation1).isEqualTo(matchRelation2);
        matchRelation2.setId(2L);
        assertThat(matchRelation1).isNotEqualTo(matchRelation2);
        matchRelation1.setId(null);
        assertThat(matchRelation1).isNotEqualTo(matchRelation2);
    }
}
