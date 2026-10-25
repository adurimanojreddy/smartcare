package com.smartcare.service;

import com.smartcare.dto.labtest.LabTestRequest;
import com.smartcare.dto.labtest.LabTestResponse;

import java.util.List;

public interface LabTestService {

    /** Module (Doctor features) - "Request lab tests". */
    LabTestResponse request(Long doctorId, LabTestRequest request);

    List<LabTestResponse> getForPatient(Long patientId);

    LabTestResponse updateResult(Long labTestId, String resultNotes, String status);
}
