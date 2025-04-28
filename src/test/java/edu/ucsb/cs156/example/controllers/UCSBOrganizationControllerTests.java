package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.http.MediaType; //changed from import com.google.common.net.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

    @MockBean
    UCSBOrganizationRepository ucsbOrganizationRepository;

    @MockBean
    UserRepository userRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/ucsborganization/all"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/ucsborganization/all"))
                .andExpect(status().is(200));
    }

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/ucsborganization/post"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/ucsborganization/post"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsbdates() throws Exception {
        // arrange
        UCSBOrganization org1 = UCSBOrganization.builder()
                .orgCode("SKY")
                .orgTranslationShort("Sky Club")
                .orgTranslation("The Sky is the Limit Club")
                .inactive(false)
                .build();

        ArrayList<UCSBOrganization> expectedOrganization = new ArrayList<>();
        expectedOrganization.add(org1);

        when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganization);

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedOrganization);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_ucsborganization() throws Exception {
        // arrange
        UCSBOrganization org1 = UCSBOrganization.builder()
                .orgCode("SKY")
                .orgTranslationShort("Sky Club")
                .orgTranslation("The Sky is the Limit Club")
                .inactive(false)
                .build();

        when(ucsbOrganizationRepository.save(eq(org1))).thenReturn(org1);

        // act
        MvcResult response = mockMvc.perform(
                post("/api/ucsborganization/post")
                        .param("orgCode", "SKY")
                        .param("orgTranslationShort", "Sky Club")
                        .param("orgTranslation", "The Sky is the Limit Club")
                        .param("inactive", "false")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).save(org1);
        String expectedJson = mapper.writeValueAsString(org1);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // NEW TEST CASE 1: Test getting multiple organizations
    @WithMockUser(roles = { "USER" })
    @Test
    public void test_get_multiple_organizations() throws Exception {
        // arrange
        UCSBOrganization org1 = UCSBOrganization.builder()
                .orgCode("SKY")
                .orgTranslationShort("Sky Club")
                .orgTranslation("The Sky is the Limit Club")
                .inactive(false)
                .build();

        UCSBOrganization org2 = UCSBOrganization.builder()
                .orgCode("OCEAN")
                .orgTranslationShort("Ocean Club")
                .orgTranslation("Deep Sea Explorers")
                .inactive(true)
                .build();

        ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
        expectedOrganizations.addAll(Arrays.asList(org1, org2));

        when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedOrganizations);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    // NEW TEST CASE 2: Test posting organization with inactive status
    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_post_inactive_organization() throws Exception {
        // arrange
        UCSBOrganization org = UCSBOrganization.builder()
                .orgCode("INACT")
                .orgTranslationShort("Inactive Club")
                .orgTranslation("Currently Inactive Organization")
                .inactive(true)
                .build();

        when(ucsbOrganizationRepository.save(eq(org))).thenReturn(org);

        // act
        MvcResult response = mockMvc.perform(
                post("/api/ucsborganization/post")
                        .param("orgCode", "INACT")
                        .param("orgTranslationShort", "Inactive Club")
                        .param("orgTranslation", "Currently Inactive Organization")
                        .param("inactive", "true")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).save(org);
        String expectedJson = mapper.writeValueAsString(org);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/ucsborganization?id=7"))
                .andExpect(status().is(403)); // logged out users can't get by id
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_exist() throws Exception {

        // arrange

        UCSBOrganization org = UCSBOrganization.builder()
                .orgCode("INACT")
                .orgTranslationShort("Inactive Club")
                .orgTranslation("Currently Inactive Organization")
                .inactive(true)
                .build();

        when(ucsbOrganizationRepository.findById(eq(7L))).thenReturn(Optional.of(org));

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganization?id=7"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(ucsbOrganizationRepository, times(1)).findById(eq(7L));
        String expectedJson = mapper.writeValueAsString(org);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

        // arrange

        when(ucsbOrganizationRepository.findById(eq(7L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/ucsborganization?id=7"))
                .andExpect(status().isNotFound()).andReturn();

        // assert

        verify(ucsbOrganizationRepository, times(1)).findById(eq(7L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("UCSBOrganization with id 7 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_ucsborganization() throws Exception {
        // arrange

        UCSBOrganization org = UCSBOrganization.builder()
                .orgCode("INACT")
                .orgTranslationShort("Inactive Club")
                .orgTranslation("Currently Inactive Organization")
                .inactive(true)
                .build();

        UCSBOrganization editedOrg = UCSBOrganization.builder()
                .orgCode("NEW-INACT")
                .orgTranslationShort("New Inactive Club")
                .orgTranslation("Currently New Inactive Organization")
                .inactive(false)
                .build();

        String requestBody = mapper.writeValueAsString(editedOrg);

        when(ucsbOrganizationRepository.findById(eq(67L))).thenReturn(Optional.of(org));

        // act
        MvcResult response = mockMvc.perform(
                put("/api/ucsborganization?id=67")
                        .contentType(MediaType.APPLICATION_JSON) // app_json import included at top
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById(67L);
        verify(ucsbOrganizationRepository, times(1)).save(editedOrg);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_ucsborganization_that_does_not_exist() throws Exception {
        // arrange

        UCSBOrganization org = UCSBOrganization.builder()
                .orgCode("INACT")
                .orgTranslationShort("Inactive Club")
                .orgTranslation("Currently Inactive Organization")
                .inactive(true)
                .build();

        String requestBody = mapper.writeValueAsString(org);

        when(ucsbOrganizationRepository.findById(eq(67L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                put("/api/ucsborganization?id=67")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById(67L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganization with id 67 not found", json.get("message"));

    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_a_organization() throws Exception {
        // arrange

        UCSBOrganization org = UCSBOrganization.builder()
                .orgCode("INACT")
                .orgTranslationShort("Inactive Club")
                .orgTranslation("Currently Inactive Organization")
                .inactive(true)
                .build();

        when(ucsbOrganizationRepository.findById(eq(15L))).thenReturn(Optional.of(org));

        // act
        MvcResult response = mockMvc.perform(
                delete("/api/ucsborganization?id=15")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById(15L);
        verify(ucsbOrganizationRepository, times(1)).delete(eq(org));

        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganization with id 15 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existant_ucsborganization_and_gets_right_error_message()
            throws Exception {
        // arrange

        when(ucsbOrganizationRepository.findById(eq(15L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                delete("/api/ucsborganization?id=15")
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(ucsbOrganizationRepository, times(1)).findById(15L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("UCSBOrganization with id 15 not found", json.get("message"));
    }

}