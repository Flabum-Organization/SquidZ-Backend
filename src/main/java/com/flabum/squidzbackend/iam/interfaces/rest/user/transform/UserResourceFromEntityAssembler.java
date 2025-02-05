package com.flabum.squidzbackend.iam.interfaces.rest.user.transform;


import com.flabum.squidzbackend.iam.domain.model.aggregates.User;
import com.flabum.squidzbackend.iam.domain.model.entities.Role;
import com.flabum.squidzbackend.iam.interfaces.rest.user.resources.AuthenticateUserResource;
import com.flabum.squidzbackend.iam.interfaces.rest.user.resources.UserResource;

public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User user){
        var roles = user.getRoles().stream().map(Role::getStringName).toList();
        return new UserResource(user.getId(), user.getName(), user.getPhoneNumber(), user.getEmail(), roles);
    }

    public static AuthenticateUserResource toResourceFromEntityAndToken(User user, String token){
        var roles = user.getRoles().stream().map(Role::getStringName).toList();
        return new AuthenticateUserResource(user.getId(), user.getEmail().address(), token);
    }
}
