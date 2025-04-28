package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.LocalDateTime;

/**
 * This is a REST controller for UCSBOrganization
 */

@Tag(name = "UCSBOrganization")
@RequestMapping("/api/ucsborganization")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController {

    @Autowired
    UCSBOrganizationRepository ucsbOrganizationRepository;

    /**
     * List all UCSB Organizations
     * 
     * @return an iterable of UCSBOrganization
     */
    @Operation(summary = "List all ucsb organizations")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBOrganization> allUCSBOrganization() {
        Iterable<UCSBOrganization> ucsborganization = ucsbOrganizationRepository.findAll();
        return ucsborganization;
    }

    /**
     * Create a new organization
     * 
     * @param orgCode             the ucsb organization code
     * @param orgTranslationShort the ucsb organization translation short
     * @param orgTranslation      the ucsb organization translation
     * @param inactive            inactive boolean
     * @return the saved ucsborganization
     */
    @Operation(summary = "Create a new organization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBOrganization postUCSBOrganization(
            @Parameter(name = "orgCode") @RequestParam String orgCode,
            @Parameter(name = "orgTranslationShort") @RequestParam String orgTranslationShort,
            @Parameter(name = "orgTranslation") @RequestParam String orgTranslation,
            @Parameter(name = "inactive") @RequestParam boolean inactive)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        // NO TIME CONSTRAINTS FOR UCSB ORGANIZATION
        // log.info("localDateTime={}", localDateTime);

        UCSBOrganization ucsbOrganization = new UCSBOrganization();
        ucsbOrganization.setOrgCode(orgCode);
        ucsbOrganization.setOrgTranslationShort(orgTranslationShort);
        ucsbOrganization.setOrgTranslation(orgTranslation);
        ucsbOrganization.setInactive(inactive);

        UCSBOrganization savedUcsbOrganization = ucsbOrganizationRepository.save(ucsbOrganization);

        return savedUcsbOrganization;
    }

    /**
     * Get a single ucsborganization by id
     * 
     * @param id the id of the ucsb organization
     * @return a UCSBOrganization
     */
    @Operation(summary = "Get a single ucsb Organization")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBOrganization getById(
            @Parameter(name = "id") @RequestParam Long id) {
        UCSBOrganization ucsbOrganization = ucsbOrganizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, id));

        return ucsbOrganization;
    }

    /**
     * Update a single ucsborganization
     * 
     * @param id       id of the ucsborganization to update
     * @param incoming the new ucsborganization
     * @return the updated ucsborganization object
     */
    @Operation(summary = "Update a single ucsborganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBOrganization updateUCSBOrganization(
            @Parameter(name = "id") @RequestParam Long id,
            @RequestBody @Valid UCSBOrganization incoming) {

        UCSBOrganization ucsbOrganization = ucsbOrganizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, id));

        ucsbOrganization.setInactive(incoming.getInactive());
        ucsbOrganization.setOrgCode(incoming.getOrgCode());
        ucsbOrganization.setOrgTranslation(incoming.getOrgTranslation());
        ucsbOrganization.setOrgTranslationShort(incoming.getOrgTranslationShort());

        ucsbOrganizationRepository.save(ucsbOrganization);

        return ucsbOrganization;
    }

    /**
     * Delete a UCSBOrganization
     * 
     * @param id the id of the ucsborganization to delete
     * @return a message indicating the ucsborganization was deleted
     */
    @Operation(summary = "Delete a UCSBOrganization")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteUCSBOrganization(
            @Parameter(name = "id") @RequestParam Long id) {
        UCSBOrganization ucsbOrganization = ucsbOrganizationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, id));

        ucsbOrganizationRepository.delete(ucsbOrganization);
        return genericMessage("UCSBOrganization with id %s deleted".formatted(id));
    }
}
