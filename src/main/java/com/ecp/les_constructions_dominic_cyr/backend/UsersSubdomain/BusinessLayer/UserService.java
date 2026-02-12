package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserCreateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserUpdateRequestModel;

import java.util.List;

public interface UserService {

    UserResponseModel createUser(UserCreateRequestModel requestModel);
    List<UserResponseModel> getAllUsers();
    UserResponseModel getUserById(String userId);
    UserResponseModel getUserByAuth0Id(String auth0UserId);
    UserResponseModel updateUser(String userId, UserUpdateRequestModel requestModel);
    UserResponseModel updateUserAsOwner(String userId, UserUpdateRequestModel requestModel, String requestingAuth0UserId);
    UserResponseModel deactivateUser(String userId, String requestingAuth0UserId);
    UserResponseModel setUserInactive(String userId, String requestingAuth0UserId);
    UserResponseModel reactivateUser(String userId, String requestingAuth0UserId);
    List<UserResponseModel> getActiveContractors();
    List<UserResponseModel> getActiveSalespersons();
    List<UserResponseModel> getActiveCustomers();
    
    /**
     * Get active customers that share at least one lot with the specified user (typically a salesperson).
     * @param auth0UserId The auth0 user ID of the salesperson
     * @return List of customers with shared lot assignments
     */
    List<UserResponseModel> getCustomersWithSharedLots(String auth0UserId);
}
