package com.dabel.service.user;

import com.dabel.app.Helper;
import com.dabel.constant.Status;
import com.dabel.dto.LoginLogDto;
import com.dabel.dto.UserDto;
import com.dabel.dto.UserLogDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.exception.ResourceNotFoundException;
import com.dabel.mapper.LoginLogMapper;
import com.dabel.mapper.UserLogMapper;
import com.dabel.mapper.UserMapper;
import com.dabel.model.Role;
import com.dabel.model.User;
import com.dabel.model.UserLog;
import com.dabel.repository.LoginLogRepository;
import com.dabel.repository.RoleRepository;
import com.dabel.repository.UserLogRepository;
import com.dabel.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final LoginLogRepository loginLogRepository;
    private final UserLogRepository userLogRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, LoginLogRepository loginLogRepository, UserLogRepository userLogRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.loginLogRepository = loginLogRepository;
        this.userLogRepository = userLogRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = getUser(username);
        setUserRole(user);

        return new CustomUserDetails(user);
    }

    public UserDto save(UserDto userDto) {
        return UserMapper.toDto(userRepository.save(UserMapper.toEntity(userDto)));
    }

    public void create(UserDto userDto) {

        //TODO: encode password, set user creator and user status
        userDto.setPassword(passwordEncoder().encode("123")); //by default, we make password to 123, user can change it later
        userDto.setStatus(Status.ACTIVE.code()); //by default user is active
        userDto.setInitiatedBy(Helper.getAuthenticated().getName());
        User savedUser = userRepository.save(UserMapper.toEntity(userDto));

        //TODO: save user roles
        roleRepository.save(new Role(savedUser, userDto.getRole().toUpperCase()));
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
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken)
            return UserDto.builder()
                    .firstName("Unknown firstname")
                    .lastName("Unknown lastName")
                    .username("anonymous")
                    .role("ROLE_ANONYMOUS")
                    .status(Status.INACTIVE.code())
                    .build();

        return findByUsername(auth.getName());
    }

    public List<LoginLogDto> getLoginLogs(UserDto userDto) {
        User user = UserMapper.toEntity(userDto);

        return loginLogRepository.findAllByUser(user).stream()
                .map(LoginLogMapper::toDTO)
                .toList();
    }

    public void saveLoginLog(LoginLogDto loginLogDto) {
        LoginLogMapper.toDTO(loginLogRepository.save(LoginLogMapper.toEntity(loginLogDto)));
    }

    public void saveLog(String httpMethod, String httpStatus, String url) {

        User user = userRepository.findByUsername(Helper.getAuthenticated().getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserLog userLog = new UserLog();
        userLog.setUser(user);
        userLog.setHttpMethod(httpMethod);
        userLog.setHttpStatus(httpStatus);
        userLog.setUrl(url);

        userLogRepository.save(userLog);
    }

    public List<UserLogDto> getLogs(UserDto userDto) {
        User user = UserMapper.toEntity(userDto);

        return userLogRepository.findAllByUser(user).stream()
                .map(UserLogMapper::toDTO)
                .toList();
    }

    public void updateUsername(UserDto userDto, String newUsername) {

        if (userRepository.existsByUsername(newUsername))
            throw new IllegalOperationException("Username already exists");

        userDto.setUsername(newUsername);
        userDto.setUpdatedBy(Helper.getAuthenticated().getName());
        save(userDto);
    }

    public void updatePassword(UserDto userDto, String newPassword) {
        userDto.setPassword(passwordEncoder().encode(newPassword));
        userDto.setUpdatedBy(Helper.getAuthenticated().getName());
        save(userDto);
    }

    public void updateRole(UserDto userDto, String newRoleName) {
        Role currentUserRole = roleRepository.findByUser(UserMapper.toEntity(userDto))
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        currentUserRole.setName(newRoleName);
        roleRepository.save(currentUserRole);
    }

    public void logoutUser(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextHolder.clearContext();  // clear actual session
        request.getSession().invalidate();  // invalidate the HTTP session
        response.addCookie(new Cookie("JSESSIONID", null));  // clear session cookie
    }

    public boolean isPasswordValid(UserDto user, String rawPassword) {
        return passwordEncoder().matches(rawPassword, user.getPassword());
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

    private PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}