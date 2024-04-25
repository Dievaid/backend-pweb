package acs.upb.web.services;

import acs.upb.web.dtos.UserAuthDTO;
import acs.upb.web.dtos.UserDataDTO;
import acs.upb.web.dtos.UserUpdateDto;
import acs.upb.web.entities.User;
import acs.upb.web.errors.UserAlreadyExistsError;
import acs.upb.web.errors.UserNotFoundError;
import acs.upb.web.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class UserService {
    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public void addUser(UserAuthDTO userDto, boolean isAdmin) throws UserAlreadyExistsError {
        User user = new User();
        Date date = new Date();

        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setCreatedAt(date);
        user.setUpdatedAt(date);
        user.setRole(isAdmin ? "ADMIN" : "USER");

        if (userRepository.existsByUsername(user.getUid())) {
            throw new UserAlreadyExistsError();
        }

        userRepository.save(user);
    }

    public String loginUser(UserAuthDTO userDto) throws UserNotFoundError {
        var user = userRepository.findByUsername(userDto.getUsername());
        if (user.isEmpty()) {
            throw new UserNotFoundError();
        }
        if (passwordEncoder.matches(userDto.getPassword(), user.get().getPassword())) {
            return this.generateJwtToken(user.get());
        }
        return "Wrong password";
    }

    public void updateUser(UserUpdateDto userDto) throws UserNotFoundError {
        var user = userRepository.findById(userDto.getUid()).orElseThrow(UserNotFoundError::new);
        user.setUpdatedAt(new Date());
        if (userDto.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        if (userDto.getImgUrl() != null) {
            user.setImgUrl(userDto.getImgUrl());
        }
        if (userDto.getUsername() != null) {
            user.setUsername(userDto.getUsername());
        }
    }

    public List<UserDataDTO> getAllUsers() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .map(user -> modelMapper.map(user, UserDataDTO.class)).toList();
    }

    public UserDataDTO getUserById(String uid) throws UserNotFoundError {
        return modelMapper.map(userRepository.findById(uid).orElseThrow(UserNotFoundError::new), UserDataDTO.class);
    }

    public void deleteUser(String uid) throws UserNotFoundError {
        if (!userRepository.existsById(uid)) {
            throw new UserNotFoundError();
        }
        userRepository.deleteById(uid);
    }

    private String generateJwtToken(User user) {
        Instant now = Instant.now();
        long expiry = 3600L;
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(user.getUid())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiry))
                .claim("id", user.getUid())
                .claim("role", user.getRole())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
