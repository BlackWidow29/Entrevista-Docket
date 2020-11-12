package br.com.docket.controller;

import br.com.docket.repository.CertificateRepository;
import br.com.docket.repository.RegistryRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RegistryController {

    private final RegistryRepository registryRepository;
    private final CertificateRepository certificateRepository;

    public RegistryController(RegistryRepository registryRepository, CertificateRepository certificateRepository) {
        this.registryRepository = registryRepository;
        this.certificateRepository = certificateRepository;
    }

    @GetMapping(value = "/allregistries")
    public String showAllRegistries(Model model){
        model.addAttribute("registries", registryRepository.findAll());
        model.addAttribute("certificates", certificateRepository.findAll());
        return "registry";
    }

}
