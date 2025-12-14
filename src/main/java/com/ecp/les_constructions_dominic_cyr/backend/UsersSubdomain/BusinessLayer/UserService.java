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
}
