package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.drugorder.*;
import org.bahmni.module.bahmnicore.model.BahmniFeedDrugOrder;
import org.openmrs.*;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BahmniDrugOrderService {
    void add(String patientId, Date orderDate, List<BahmniFeedDrugOrder> bahmniDrugOrders, String systemUserName, String visitTypeName);
    List<DrugOrder> getActiveDrugOrders(String patientUuid, Date startDate, Date endDate);

    List<DrugOrder> getActiveDrugOrders(String patientUuid);

    List<DrugOrder> getActiveDrugOrders(String patientUuid, Set<Concept> conceptsToFilter, Set<Concept> conceptsToExclude);

    List<DrugOrder> getPrescribedDrugOrders(List<String> visitUuids, String patientUuid, Boolean includeActiveVisit, Integer numberOfVisit, Date startDate, Date endDate, Boolean getEffectiveOrdersOnly);

    List<DrugOrder> getPrescribedDrugOrdersForConcepts(Patient patient, Boolean includeActiveVisit, List<Visit> visits, List<Concept> concepts, Date startDate, Date endDate);

    DrugOrderConfigResponse getConfig();

    List<Order> getAllDrugOrders(String patientUuid, String patientProgramUuid, Set<Concept> conceptsForDrugs, Set<Concept> drugConceptsToBeExcluded) throws ParseException;

    Map<String,DrugOrder> getDiscontinuedDrugOrders(List<DrugOrder> drugOrders);

    List<DrugOrder> getInactiveDrugOrders(String patientUuid, Set<Concept> concepts, Set<Concept> drugConceptsToBeExcluded);
}
