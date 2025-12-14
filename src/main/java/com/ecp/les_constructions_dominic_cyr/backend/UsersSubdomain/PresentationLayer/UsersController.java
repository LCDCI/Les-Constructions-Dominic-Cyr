package com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.PresentationLayer;

import com.ecp.les_constructions_dominic_cyr.backend.UsersSubdomain.BusinessLayer.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<UserResponseModel>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseModel> getUserById(@PathVariable String userId) {
        UserResponseModel responseModel = userService.getUserById(userId);
        return ResponseEntity.ok(responseModel);
    }

    @GetMapping("/auth0/{auth0UserId}")
    public ResponseEntity<UserResponseModel> getUserByAuth0Id(@PathVariable String auth0UserId) {
        UserResponseModel responseModel = userService.getUserByAuth0Id(auth0UserId);
        return ResponseEntity.ok(responseModel);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseModel> updateUser(
            @PathVariable String userId,
            @RequestBody UserUpdateRequestModel requestModel) {
        UserResponseModel responseModel = userService.updateUser(userId, requestModel);
        return ResponseEntity.ok(responseModel);
    }
}
