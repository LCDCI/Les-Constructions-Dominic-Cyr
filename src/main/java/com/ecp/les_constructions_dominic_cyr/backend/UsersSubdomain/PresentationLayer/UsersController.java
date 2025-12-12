package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseModel> createUser(@RequestBody UserCreateRequestModel requestModel) {
        UserResponseModel responseModel = userService.createUser(requestModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseModel);
    }
}
