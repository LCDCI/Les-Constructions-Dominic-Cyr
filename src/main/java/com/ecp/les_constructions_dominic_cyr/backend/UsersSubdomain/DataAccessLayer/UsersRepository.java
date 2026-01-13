package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsersRepository extends JpaRepository<Users, UserIdentifier> {

    Optional<Users> findByPrimaryEmail(String primaryEmail);

    Optional<Users> findByAuth0UserId(String auth0UserId);

    Optional<Users> findByUserIdentifier_UserId(UUID userId);

    List<Users> findByUserStatus(UserStatus userStatus);

    List<Users> findByUserStatusIn(List<UserStatus> statuses);
}
