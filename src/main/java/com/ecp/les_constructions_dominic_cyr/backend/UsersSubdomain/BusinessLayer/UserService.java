package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserCreateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;

public interface UserService {

    UserResponseModel createUser(UserCreateRequestModel requestModel);
}
