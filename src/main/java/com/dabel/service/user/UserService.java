package com.dabel.service.user;

import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.dto.UserDto;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.UserMapper;
import com.dabel.model.Role;
import com.dabel.model.User;
import com.dabel.repository.RoleRepository;
import com.dabel.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = getUser(username);

        //TODO: set user role
        this.setUserRole(user);
        return new CustomUserDetails(user);
    }

    public UserDto save(UserDto userDto) {
        return UserMapper.toDto(userRepository.save(UserMapper.toModel(userDto)));
    }

    public void create(UserDto userDto) {

        //TODO: encode password, set user creator and user status
        userDto.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("123")); //by default, we make password to 123, user can change it later
        userDto.setStatus(Status.ACTIVE.code()); //by default user is active
        userDto.setInitiatedBy(Helper.getAuthenticated().getName());
        User savedUser = userRepository.save(UserMapper.toModel(userDto));

        //TODO: save user roles
        roleRepository.save(new Role(savedUser, String.format("ROLE_%s", userDto.getRole()).toUpperCase()));
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .peek(this::setUserRole)
                .map(UserMapper::toDto)
                .toList();
    }

    public UserDto findByUsername(String username) {
        User user = getUser(username);
        setUserRole(user);

        return UserMapper.toDto(user);
    }
    public void incrementFailedLoginAttempts(String username) {
        User user = getUser(username);
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        userRepository.save(user);
    }

    public UserDto getAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return this.findByUsername(auth.getName());
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }

    private void setUserRole(User user) {
        user.setRole(roleRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"))
                .getName());
    }

}