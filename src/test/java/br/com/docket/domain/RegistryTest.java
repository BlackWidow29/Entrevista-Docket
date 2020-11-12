package br.com.docket.domain;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import br.com.docket.web.rest.TestUtil;

public class RegistryTest {

    @Test
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Registry.class);
        Registry registry1 = new Registry();
        registry1.setId(1L);
        Registry registry2 = new Registry();
        registry2.setId(registry1.getId());
        assertThat(registry1).isEqualTo(registry2);
        registry2.setId(2L);
        assertThat(registry1).isNotEqualTo(registry2);
        registry1.setId(null);
        assertThat(registry1).isNotEqualTo(registry2);
    }
}
