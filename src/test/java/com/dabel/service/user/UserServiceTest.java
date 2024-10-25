package com.dabel.service.user;

import com.dabel.dto.LoginLogDto;
import com.dabel.dto.UserDto;
import com.dabel.dto.UserLogDto;
import com.dabel.exception.IllegalOperationException;
import com.dabel.mapper.LoginLogMapper;
import com.dabel.mapper.UserMapper;
import com.dabel.model.Role;
import com.dabel.model.User;
import com.dabel.model.UserLog;
import com.dabel.repository.LoginLogRepository;
import com.dabel.repository.RoleRepository;
import com.dabel.repository.UserLogRepository;
import com.dabel.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private LoginLogRepository loginLogRepository;

    @Mock
    private UserLogRepository userLogRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        // Setup initial user DTO and entity
        userDto = UserDto.builder()
                .username("testUser")
                .password(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("123"))
                .role("ROLE_USER")
                .build();
        user = UserMapper.toEntity(userDto);
    }

    @Test
    void shouldSaveUser() {
        // Given: mock user to be saved
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When: Save the user
        UserDto savedUser = userService.save(userDto);

        // Then: Assert that the user was saved correctly
        assertNotNull(savedUser);
        assertEquals("testUser", savedUser.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldCreateUserWithHisRole() {
        // Given: mock user and role to be saved
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(roleRepository.save(any(Role.class))).thenReturn(new Role());

        // When: Create user
        userService.create(userDto);

        // Then: Assert that user and role were saved
        verify(userRepository).save(any(User.class));
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldReturnAllUsers() {
        // Given: A role associated with the user
        Role role = new Role();
        role.setUser(user);
        role.setName("ROLE_MANAGER");

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(roleRepository.findByUser(user)).thenReturn(Optional.of(role));

        // When
        List<UserDto> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testUser", result.get(0).getUsername());
        assertEquals("ROLE_MANAGER", result.get(0).getRole());
    }

    @Test
    void shouldReturnUserDtoWhenUserExists() {
        // Given: Existing username
        String username = "testUser";

        Role role = new Role();
        role.setUser(user);
        role.setName("ROLE_MANAGER");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(roleRepository.findByUser(user)).thenReturn(Optional.of(role));

        // When
        UserDto result = userService.findByUsername(username);

        // Then
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals("ROLE_MANAGER", result.getRole());
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
        when(userRepository.findByUsername("unknownUser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userService.findByUsername("unknownUser"));
    }

    @Test
    void shouldThrowUsernameNotFoundExceptionWhenLoadUserDoesNotExist() {
        String nonExistentUsername = "nonExistentUser";
        when(userRepository.findByUsername(nonExistentUsername)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(nonExistentUsername));
    }


    @Test
    void shouldIncrementFailedLoginAttemptsWhenCalled() {
        // Given: User with zero failed login attempts
        user.setFailedLoginAttempts(0);
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        // When: Increment failed login attempts
        userService.incrementFailedLoginAttempts("testUser");

        // Then: Assert that attempts are incremented
        assertEquals(1, user.getFailedLoginAttempts());
        verify(userRepository).save(user);
    }

    @Test
    void shouldReturnAuthenticatedUserDtoWhenUserIsLoggedIn() {
        // Given: set up and mock an authenticated user
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testUser");
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        user.setFirstName("Test");
        user.setLastName("User");
        Role role = new Role();
        role.setUser(user);
        role.setName("ROLE_MANAGER");

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(roleRepository.findByUser(user)).thenReturn(Optional.of(role));

        // When: Get authenticated user
        UserDto result = userService.getAuthenticated();

        // Then
        assertEquals("testUser", result.getUsername());
        assertEquals("Test", result.getFirstName());
        assertEquals("User", result.getLastName());
        assertEquals("ROLE_MANAGER", result.getRole());
    }

    @Test
    void shouldReturnAnonymousUserWhenNoUserIsAuthenticated() {
        // Given: set up an authenticated user
        Authentication anonymousAuth = mock(AnonymousAuthenticationToken.class);
        when(anonymousAuth.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(anonymousAuth);

        // When
        UserDto result = userService.getAuthenticated();

        // Then
        assertNotNull(result);
        assertEquals("anonymous", result.getUsername());
        assertEquals("Unknown firstname", result.getFirstName());
        assertEquals("Unknown lastName", result.getLastName());
        assertEquals("ROLE_ANONYMOUS", result.getRole());
    }

    @Test
    void shouldReturnLoginLogsWhenUserExists() {
        // Given
        when(loginLogRepository.findAllByUser(any(User.class))).thenReturn(new ArrayList<>());

        // When
        List<LoginLogDto> foundLogs = userService.getLoginLogs(userDto);

        // Then
        assertNotNull(foundLogs);
        assertTrue(foundLogs.isEmpty());
    }

    @Test
    void shouldSaveLoginLogSuccessfully() {
        // Given
        LoginLogDto loginLogDto = new LoginLogDto();
        when(loginLogRepository.save(any())).thenReturn(LoginLogMapper.toEntity(loginLogDto));

        // When
        userService.saveLoginLog(loginLogDto);

        // Then
        verify(loginLogRepository).save(any());
    }

    @Test
    void shouldSaveLogSuccessfullyWhenUserIsAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("testUser");
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        userService.saveLog("GET", "200", "/some/url");

        verify(userLogRepository).save(any(UserLog.class));
    }

    @Test
    void shouldReturnEmptyLogsWhenNoneFound() {
        when(userLogRepository.findAllByUser(any(User.class))).thenReturn(new ArrayList<>());

        List<UserLogDto> foundLogs = userService.getLogs(userDto);

        assertNotNull(foundLogs);
        assertTrue(foundLogs.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenUsernameIsTaken() {
        String existingUsername = "testUser";
        when(userRepository.existsByUsername(existingUsername)).thenReturn(true);

        boolean result = userService.isUsernameTaken(existingUsername);

        assertTrue(result);
        verify(userRepository).existsByUsername(existingUsername);
    }

    @Test
    void shouldReturnFalseWhenUsernameIsNotTaken() {

        String newUsername = "newUser";
        when(userRepository.existsByUsername(newUsername)).thenReturn(false);

        boolean result = userService.isUsernameTaken(newUsername);

        assertFalse(result);
        verify(userRepository).existsByUsername(newUsername);
    }


    @Test
    void shouldUpdateUsername() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("newUser")).thenReturn(false);

        userService.updateUsername(userDto, "newUser");

        assertEquals("newUser", userDto.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdatePassword() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        userService.updatePassword(userDto, "newPassword");

        assertNotEquals("123", userDto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateRoleWhenRoleExists() {
        Role role = new Role(user, "ROLE_USER");
        when(roleRepository.findByUser(any(User.class))).thenReturn(Optional.of(role));

        userService.updateRole(userDto, "ROLE_ADMIN");

        assertEquals("ROLE_ADMIN", role.getName());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void shouldLogoutUserSuccessfully() {
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        userService.logoutUser(request, response);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(session).invalidate();
        verify(response).addCookie(argThat(cookie -> "JSESSIONID".equals(cookie.getName()) && cookie.getValue() == null));
    }

    @Test
    void shouldReturnTrueWhenPasswordIsValid() {
        String rawPassword = "myPassword";
        userDto.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(rawPassword));

        boolean result = userService.isPasswordValid(userDto, rawPassword);

        assertTrue(result);
    }

    @Test
    void shouldReturnFalseWhenInvalidPassword() {
        userDto.setPassword(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("myPassword"));

        boolean result = userService.isPasswordValid(userDto, "wrongPassword");

        assertFalse(result);
    }
}