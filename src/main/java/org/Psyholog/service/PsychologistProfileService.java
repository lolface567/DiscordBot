package org.Psyholog.service;

import org.Psyholog.entity.PsychologistProfile;
import org.Psyholog.repository.PsychologistProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PsychologistProfileService {
    private final PsychologistProfileRepository repository;

    @Autowired
    public PsychologistProfileService(PsychologistProfileRepository repository) {
        this.repository = repository;
    }

    public void saveOrUpdate(String userId, String username) {
        PsychologistProfile profile = repository.findById(userId)
                .orElse(new PsychologistProfile(userId, username));

        profile.setUsername(username);

        repository.save(profile);
    }
}
