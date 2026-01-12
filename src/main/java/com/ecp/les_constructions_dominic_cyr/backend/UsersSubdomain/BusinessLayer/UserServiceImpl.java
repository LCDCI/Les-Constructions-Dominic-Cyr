package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.*;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.MapperLayer.UserMapper;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserCreateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserUpdateRequestModel;
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
                .filter(user -> user.getUserStatus() != UserStatus.DEACTIVATED)
                .map(user -> UserMapper.toResponseModel(user, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseModel getUserById(String userId) {
        UserIdentifier userIdentifier = UserIdentifier.fromString(userId);
        Users user = usersRepository.findById(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return UserMapper.toResponseModel(user, null);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseModel getUserByAuth0Id(String auth0UserId) {
        Users user = usersRepository.findByAuth0UserId(auth0UserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with Auth0 ID: " + auth0UserId));
        return UserMapper.toResponseModel(user, null);
    }

    @Override
    @Transactional
    public UserResponseModel updateUser(String userId, UserUpdateRequestModel requestModel) {
        UserIdentifier userIdentifier = UserIdentifier.fromString(userId);
        Users user = usersRepository.findById(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        // Update only the allowed fields, checking for non-null and non-empty values
        if (requestModel.getFirstName() != null && !requestModel.getFirstName().trim().isEmpty()) {
            user.setFirstName(requestModel.getFirstName().trim());
        }
        if (requestModel.getLastName() != null && !requestModel.getLastName().trim().isEmpty()) {
            user.setLastName(requestModel.getLastName().trim());
        }
        if (requestModel.getPhone() != null && !requestModel.getPhone().trim().isEmpty()) {
            user.setPhone(requestModel.getPhone().trim());
        }
        if (requestModel.getSecondaryEmail() != null && !requestModel.getSecondaryEmail().trim().isEmpty()) {
            user.setSecondaryEmail(requestModel.getSecondaryEmail().trim());
        }

        user = usersRepository.save(user);
        return UserMapper.toResponseModel(user, null);
    }

    @Override
    @Transactional
    public UserResponseModel updateUserAsOwner(String userId, UserUpdateRequestModel requestModel, String requestingAuth0UserId) {
        Users requestingUser = usersRepository.findByAuth0UserId(requestingAuth0UserId)
            .orElseThrow(() -> new IllegalArgumentException("Requesting user not found"));

    if (requestingUser.getUserRole() != UserRole.OWNER) {
        throw new SecurityException("Only OWNER users can update other user accounts");
    }

    UserIdentifier userIdentifier = UserIdentifier.fromString(userId);
    Users user = usersRepository.findById(userIdentifier)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    boolean nameChanged = false;
    boolean emailChanged = false;

    if (requestModel.getFirstName() != null && !requestModel.getFirstName().trim().isEmpty()) {
        user.setFirstName(requestModel.getFirstName().trim());
        nameChanged = true;
    }
    if (requestModel.getLastName() != null && !requestModel.getLastName().trim().isEmpty()) {
        user.setLastName(requestModel.getLastName().trim());
        nameChanged = true;
    }
    if (requestModel.getPhone() != null && !requestModel.getPhone().trim().isEmpty()) {
        user.setPhone(requestModel.getPhone().trim());
    }
    if (requestModel.getPrimaryEmail() != null && !requestModel.getPrimaryEmail().trim().isEmpty()) {
        if (usersRepository.findByPrimaryEmail(requestModel.getPrimaryEmail().trim()).isPresent()
            && !user.getPrimaryEmail().equals(requestModel.getPrimaryEmail().trim())) {
            throw new IllegalArgumentException("A user with this primary email already exists.");
        }
        user.setPrimaryEmail(requestModel.getPrimaryEmail().trim());
        emailChanged = true;
    }
    if (requestModel.getSecondaryEmail() != null && !requestModel.getSecondaryEmail().trim().isEmpty()) {
        user.setSecondaryEmail(requestModel.getSecondaryEmail().trim());
    }

    if (user.getAuth0UserId() != null) {
        try {
            if (nameChanged || emailChanged) {
                auth0ManagementService.updateAuth0UserEmailAndName(
                    user.getAuth0UserId(),
                    emailChanged ? user.getPrimaryEmail() : null,
                    user.getFirstName(),
                    user.getLastName()
                );
            }
        } catch (Exception e) {
        }
    }

    user = usersRepository.save(user);
    return UserMapper.toResponseModel(user, null);
}

    @Override
    @Transactional
    public UserResponseModel deactivateUser(String userId, String requestingAuth0UserId) {
        Users requestingUser = usersRepository. findByAuth0UserId(requestingAuth0UserId)
                .orElseThrow(() -> new IllegalArgumentException("Requesting user not found"));

        if (requestingUser.getUserRole() != UserRole.OWNER) {
            throw new SecurityException("Only OWNER users can deactivate accounts");
        }

        UserIdentifier userIdentifier = UserIdentifier.fromString(userId);
        Users user = usersRepository.findById(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (user.getUserRole() == UserRole.OWNER) {
            throw new IllegalArgumentException("Cannot deactivate OWNER accounts");
        }

        user. setUserStatus(UserStatus. DEACTIVATED);

        if (user.getAuth0UserId() != null) {
            try {
                auth0ManagementService.blockAuth0User(user.getAuth0UserId(), true);
            } catch (Exception e) {
            }
        }

        user = usersRepository.save(user);
        return UserMapper.toResponseModel(user, null);
    }

    @Override
    @Transactional
    public UserResponseModel setUserInactive(String userId, String requestingAuth0UserId) {
        Users requestingUser = usersRepository.findByAuth0UserId(requestingAuth0UserId)
                .orElseThrow(() -> new IllegalArgumentException("Requesting user not found"));

        if (requestingUser.getUserRole() != UserRole.OWNER) {
            throw new SecurityException("Only OWNER users can set accounts as inactive");
        }

        UserIdentifier userIdentifier = UserIdentifier.fromString(userId);
        Users user = usersRepository. findById(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (user.getUserRole() == UserRole.OWNER) {
            throw new IllegalArgumentException("Cannot set OWNER accounts as inactive");
        }

        user. setUserStatus(UserStatus. INACTIVE);
        user = usersRepository.save(user);
        return UserMapper.toResponseModel(user, null);
    }

    @Override
    @Transactional
    public UserResponseModel reactivateUser(String userId, String requestingAuth0UserId) {
        Users requestingUser = usersRepository.findByAuth0UserId(requestingAuth0UserId)
                .orElseThrow(() -> new IllegalArgumentException("Requesting user not found"));

        if (requestingUser.getUserRole() != UserRole.OWNER) {
            throw new SecurityException("Only OWNER users can reactivate accounts");
        }

        UserIdentifier userIdentifier = UserIdentifier. fromString(userId);
        Users user = usersRepository.findById(userIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.setUserStatus(UserStatus. ACTIVE);

        if (user.getAuth0UserId() != null) {
            try {
                auth0ManagementService.blockAuth0User(user.getAuth0UserId(), false);
            } catch (Exception e) {
                //we should eventually add an exception for failure to block auth0 user, but auth0 already does it in the dashboard...
            }
        }

        user = usersRepository.save(user);
        return UserMapper.toResponseModel(user, null);
    }
}