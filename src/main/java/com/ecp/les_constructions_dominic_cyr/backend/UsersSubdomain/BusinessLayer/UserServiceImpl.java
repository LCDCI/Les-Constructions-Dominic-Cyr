package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UsersRepository;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.MapperLayer.UserMapper;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserCreateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.utils.Auth0ManagementService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final Auth0ManagementService auth0ManagementService;

    @Value("${app.invite.result-url}")
    private String inviteResultUrl;

    public UserServiceImpl(UsersRepository usersRepository,
                           Auth0ManagementService auth0ManagementService) {
        this.usersRepository = usersRepository;
        this.auth0ManagementService = auth0ManagementService;
    }

    @Override
    @Transactional
    public UserResponseModel createUser(UserCreateRequestModel requestModel) {

        // Check if a user with the given primary email already exists
        if (usersRepository.findByPrimaryEmail(requestModel.getPrimaryEmail()).isPresent()) {
            throw new IllegalArgumentException("A user with this email already exists.");
        }
        // 1. Persist Users entity (without auth0UserId)
        Users userEntity = UserMapper.toEntity(requestModel);
        userEntity = usersRepository.save(userEntity);

        String roleString = userEntity.getUserRole().name();

        // 2. Create user in Auth0 using primary + secondary email
        String auth0UserId = auth0ManagementService.createAuth0User(
                userEntity.getPrimaryEmail(),
                userEntity.getSecondaryEmail(),
                userEntity.getFirstName(),
                userEntity.getLastName(),
                roleString,
                userEntity.getUserIdentifier().getUserId().toString()
        );

        // 3. Assign role in Auth0
        auth0ManagementService.assignRoleToUser(auth0UserId, roleString);

        // 4. Update entity with auth0UserId
        userEntity.setAuth0UserId(auth0UserId);
        userEntity = usersRepository.save(userEntity);

        // 5. Generate password-change ticket (invite link)
        String inviteLink = auth0ManagementService.createPasswordChangeTicket(
                auth0UserId,
                inviteResultUrl
        );

        return UserMapper.toResponseModel(userEntity, inviteLink);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseModel> getAllUsers() {
        return usersRepository.findAll()
                .stream()
                .map(user -> UserMapper.toResponseModel(user, null))
                .toList();
    }

}
