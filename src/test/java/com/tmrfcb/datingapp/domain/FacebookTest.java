package com.tmrfcb.datingapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.tmrfcb.datingapp.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class FacebookTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Facebook.class);
        Facebook facebook1 = new Facebook();
        facebook1.setId(1L);
        Facebook facebook2 = new Facebook();
        facebook2.setId(facebook1.getId());
        assertThat(facebook1).isEqualTo(facebook2);
        facebook2.setId(2L);
        assertThat(facebook1).isNotEqualTo(facebook2);
        facebook1.setId(null);
        assertThat(facebook1).isNotEqualTo(facebook2);
    }
}
