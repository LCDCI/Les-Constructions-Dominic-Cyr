package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.MapperLayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.UserIdentifier;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.DataAccessLayer.Users;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserCreateRequestModel;
import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer.UserResponseModel;

public class UserMapper {

    public static Users toEntity(UserCreateRequestModel model) {
        Users entity = new Users();
        entity.setUserIdentifier(UserIdentifier.newId());
        entity.setFirstName(model.getFirstName());
        entity.setLastName(model.getLastName());
        entity.setPrimaryEmail(model.getPrimaryEmail());
        entity.setSecondaryEmail(model.getSecondaryEmail());
        entity.setPhone(model.getPhone());
        entity.setUserRole(model.getUserRole());
        return entity;
    }

    public static UserResponseModel toResponseModel(Users entity, String inviteLink) {
        UserResponseModel model = new UserResponseModel();

        if (entity.getUserIdentifier() != null && entity.getUserIdentifier().getUserId() != null) {
            model.setUserIdentifier(entity.getUserIdentifier().getUserId().toString());
        }

        model.setFirstName(entity.getFirstName());
        model.setLastName(entity.getLastName());
        model.setPrimaryEmail(entity.getPrimaryEmail());
        model.setSecondaryEmail(entity.getSecondaryEmail());
        model.setPhone(entity.getPhone());
        model.setUserRole(entity.getUserRole());
        model.setInviteLink(inviteLink);

        return model;
    }
}
