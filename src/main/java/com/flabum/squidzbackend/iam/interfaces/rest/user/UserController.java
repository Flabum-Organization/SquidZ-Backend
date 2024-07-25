package com.flabum.squidzbackend.iam.interfaces.rest.user;

import com.flabum.squidzbackend.iam.domain.services.UserCommandService;
import com.flabum.squidzbackend.iam.infrastructure.token.jwts.services.TokenServiceImpl;
import com.flabum.squidzbackend.iam.interfaces.rest.user.resources.*;
import com.flabum.squidzbackend.iam.interfaces.rest.user.transform.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping(value = "api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name="Users", description = "Users Management Endpoints")
public class UserController {

    private final UserCommandService userCommandService;

    @PostMapping("sign-up")
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        var user = userCommandService.execute(signUpCommand);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    @GetMapping("sign-in")
    public ResponseEntity<AuthenticateUserResource> signIn(@RequestParam String email, @RequestParam String password, HttpServletResponse response, HttpServletRequest request) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(new SignInResource(email, password));
        var user = userCommandService.execute(signInCommand);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var token = user.get().right;
        var authenticatedUserResource = UserResourceFromEntityAssembler.toResourceFromEntityAndToken(user.get().left, user.get().right);
        var userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        if (userAgent != null && !userAgent.contains("Android") && !userAgent.contains("iPhone") && !userAgent.contains("iPad")) {
            TokenServiceImpl.saveJwtInCookie(request, response, token);
        }
        return ResponseEntity.ok(authenticatedUserResource);
    }

    @PutMapping("update-password")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordResource updatePasswordResource, HttpServletRequest request) {
        var updatePasswordCommand = UpdatePasswordCommandFromResourceAssembler.toCommandFromResource(updatePasswordResource);
        var isUpdatedPassword = userCommandService.execute(updatePasswordCommand, request);
        if (!isUpdatedPassword) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password update failed");
        }
        return ResponseEntity.ok("Password updated successfully");
    }

    @PutMapping("update-user-data")
    public ResponseEntity<String> updateUserData(@RequestBody UpdateUserDataResource updateUserDataResource, HttpServletRequest request) {
        var updateUserDataCommand = UpdateUserDataCommandFromResourceAssembler.toCommandFromResource(updateUserDataResource);
        var isUpdatedUserData = userCommandService.execute(updateUserDataCommand, request);
        if (!isUpdatedUserData) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User update failed");
        }
        return ResponseEntity.ok("User updated successfully");
    }
}
