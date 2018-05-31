package org.bahmni.module.bahmnicore.contract.patient.mapper;

import org.bahmni.module.bahmnicore.contract.patient.response.PatientResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.visitlocation.BahmniVisitLocationServiceImpl;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class PatientResponseMapperTest {

    private PatientResponseMapper patientResponseMapper;

    @Mock
    VisitService visitService;

    @Mock
    BahmniVisitLocationServiceImpl bahmniVisitLocationService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private Concept concept;

    @Mock
    private ConceptDatatype conceptDatatype;

    @Mock
    private ConceptName conceptName;

    Patient patient;

    @Before
    public void setUp() throws Exception {
        patient = new Patient();
        Location location = new Location(1);
        mockStatic(Context.class);
        Visit visit = new Visit(1);
        visit.setUuid("someLocationUUid");
        visit.setLocation(location);
        List<Visit> visits = new ArrayList<>();
        visits.add(visit);
        when(visitService.getActiveVisitsByPatient(patient)).thenReturn(visits);
        when(Context.getVisitService()).thenReturn(visitService);
        when(bahmniVisitLocationService.getVisitLocation(any(String.class))).thenReturn(location);
        when(Context.getConceptService()).thenReturn(conceptService);

        patientResponseMapper = new PatientResponseMapper(Context.getVisitService(), bahmniVisitLocationService);
        patient.setPatientId(12);
        PatientIdentifier primaryIdentifier = new PatientIdentifier("FAN007", new PatientIdentifierType(), new Location(1));
        PatientIdentifier extraIdentifier = new PatientIdentifier("Extra009", new PatientIdentifierType(), new Location(1));
        extraIdentifier.getIdentifierType().setName("test");
        primaryIdentifier.setPreferred(true);
        patient.setIdentifiers(Sets.newSet(primaryIdentifier, extraIdentifier));

    }

    @Test
    public void shouldMapPatientBasicDetails() throws Exception {
        patient.setBirthdate(new Date(2000000l));
        patient.setUuid("someUUid");

        PatientResponse patientResponse = patientResponseMapper.map(patient, null, null, null, null);

        Assert.assertEquals(patientResponse.getPersonId(), 12);
        Assert.assertEquals(patientResponse.getBirthDate().getTime(), 2000000l);
        Assert.assertEquals(patientResponse.getUuid(), "someUUid");
        Assert.assertEquals(patientResponse.getIdentifier(), "FAN007");
        Assert.assertEquals(patientResponse.getExtraIdentifiers(), "{\"test\" : \"Extra009\"}");
    }

    @Test
    public void shouldMapPersonAttributes() throws Exception {
        when(conceptService.getConceptByName("givenNameLocal")).thenReturn(null);
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("givenNameLocal");
        patient.setAttributes(Sets.newSet(new PersonAttribute(personAttributeType,"someName")));
        String[] patientResultFields = {"givenNameLocal"};
        PatientResponse patientResponse = patientResponseMapper.map(patient, null, patientResultFields, null, null);

        Assert.assertEquals(patientResponse.getCustomAttribute(),"{\"givenNameLocal\" : \"someName\"}");
    }

    @Test
    public void shouldAddSlashToSupportSpecialCharactersInJSON() throws Exception {
        when(conceptService.getConceptByName("familyNameLocal")).thenReturn(null);
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("familyNameLocal");
        patient.setAttributes(Sets.newSet(new PersonAttribute(personAttributeType,"so\"me\\Name")));
        String[] patientResultFields = {"familyNameLocal"};
        PatientResponse patientResponse = patientResponseMapper.map(patient, null, patientResultFields, null, null);

        Assert.assertEquals(patientResponse.getCustomAttribute(),"{\"familyNameLocal\" : \"so\\\"me\\\\Name\"}");
    }

    @Test
    public void shouldReturnAnswerValueForCodedPersonAttributes() {
        when(conceptService.getConceptByName("Education Details")).thenReturn(concept);
        when(concept.getDatatype()).thenReturn(conceptDatatype);
        when(conceptDatatype.getName()).thenReturn("Coded");
        when(conceptService.getConcept(412)).thenReturn(concept);
        when(concept.getName()).thenReturn(conceptName);
        when(conceptName.getName()).thenReturn("10th pass");
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName("Education Details");
        patient.setAttributes(Sets.newSet(new PersonAttribute(personAttributeType,"412")));
        String[] patientResultFields = {"Education Details"};
        PatientResponse patientResponse = patientResponseMapper.map(patient, null, patientResultFields, null, null);

        Assert.assertEquals("{\"Education Details\" : \"10th pass\"}", patientResponse.getCustomAttribute());
    }

    @Test
    public void shouldMapPatientAddress() throws Exception {
        PersonAddress personAddress= new PersonAddress(2);
        personAddress.setAddress2("someAddress");
        patient.setAddresses(Sets.newSet(personAddress));

        PatientResponse patientResponse = patientResponseMapper.map(patient, null, null, new String[]{"address_2"}, null);
        Assert.assertEquals(patientResponse.getAddressFieldValue(),"{\"address_2\" : \"someAddress\"}");

    }

    @Test
    public void shouldMapVisitSummary() throws Exception {

        PatientResponse patientResponse = patientResponseMapper.map(patient, null, null, null, null);
        Assert.assertEquals(patientResponse.getActiveVisitUuid(),"someLocationUUid");
        Assert.assertEquals(patientResponse.getHasBeenAdmitted(), Boolean.FALSE);
    }
}