package com.nexblog.backend.service;

import com.nexblog.backend.dto.personal.PersonalRequest;
import com.nexblog.backend.dto.personal.PersonalResponse;

public interface PersonalService {

    PersonalResponse getStudioPersonalConfig();

    PersonalResponse updatePersonalConfig(PersonalRequest request);

    PersonalResponse getPersonalConfig();
}
