package br.com.docket.web.rest;

import br.com.docket.InterviewApp;
import br.com.docket.domain.Certificate;
import br.com.docket.repository.CertificateRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link CertificateResource} REST controller.
 */
@SpringBootTest(classes = InterviewApp.class)
@AutoConfigureMockMvc
@WithMockUser
public class CertificateResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCertificateMockMvc;

    private Certificate certificate;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Certificate createEntity(EntityManager em) {
        Certificate certificate = new Certificate()
            .name(DEFAULT_NAME);
        return certificate;
    }
    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Certificate createUpdatedEntity(EntityManager em) {
        Certificate certificate = new Certificate()
            .name(UPDATED_NAME);
        return certificate;
    }

    @BeforeEach
    public void initTest() {
        certificate = createEntity(em);
    }

    @Test
    @Transactional
    public void createCertificate() throws Exception {
        int databaseSizeBeforeCreate = certificateRepository.findAll().size();
        // Create the Certificate
        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isCreated());

        // Validate the Certificate in the database
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeCreate + 1);
        Certificate testCertificate = certificateList.get(certificateList.size() - 1);
        assertThat(testCertificate.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void createCertificateWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = certificateRepository.findAll().size();

        // Create the Certificate with an existing ID
        certificate.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restCertificateMockMvc.perform(post("/api/certificates")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        // Validate the Certificate in the database
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllCertificates() throws Exception {
        // Initialize the database
        certificateRepository.saveAndFlush(certificate);

        // Get all the certificateList
        restCertificateMockMvc.perform(get("/api/certificates?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(certificate.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)));
    }
    
    @Test
    @Transactional
    public void getCertificate() throws Exception {
        // Initialize the database
        certificateRepository.saveAndFlush(certificate);

        // Get the certificate
        restCertificateMockMvc.perform(get("/api/certificates/{id}", certificate.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(certificate.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME));
    }
    @Test
    @Transactional
    public void getNonExistingCertificate() throws Exception {
        // Get the certificate
        restCertificateMockMvc.perform(get("/api/certificates/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateCertificate() throws Exception {
        // Initialize the database
        certificateRepository.saveAndFlush(certificate);

        int databaseSizeBeforeUpdate = certificateRepository.findAll().size();

        // Update the certificate
        Certificate updatedCertificate = certificateRepository.findById(certificate.getId()).get();
        // Disconnect from session so that the updates on updatedCertificate are not directly saved in db
        em.detach(updatedCertificate);
        updatedCertificate
            .name(UPDATED_NAME);

        restCertificateMockMvc.perform(put("/api/certificates")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(updatedCertificate)))
            .andExpect(status().isOk());

        // Validate the Certificate in the database
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeUpdate);
        Certificate testCertificate = certificateList.get(certificateList.size() - 1);
        assertThat(testCertificate.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    public void updateNonExistingCertificate() throws Exception {
        int databaseSizeBeforeUpdate = certificateRepository.findAll().size();

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCertificateMockMvc.perform(put("/api/certificates")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(certificate)))
            .andExpect(status().isBadRequest());

        // Validate the Certificate in the database
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteCertificate() throws Exception {
        // Initialize the database
        certificateRepository.saveAndFlush(certificate);

        int databaseSizeBeforeDelete = certificateRepository.findAll().size();

        // Delete the certificate
        restCertificateMockMvc.perform(delete("/api/certificates/{id}", certificate.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Certificate> certificateList = certificateRepository.findAll();
        assertThat(certificateList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
