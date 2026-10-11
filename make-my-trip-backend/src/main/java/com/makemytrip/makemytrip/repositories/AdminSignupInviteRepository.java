package com.makemytrip.makemytrip.repositories;

import com.makemytrip.makemytrip.models.AdminSignupInvite;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AdminSignupInviteRepository extends MongoRepository<AdminSignupInvite, String> {}
