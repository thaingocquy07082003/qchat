package com.qchat.qchat.security;

import com.qchat.qchat.auth.repository.UserRepository;
import com.qchat.qchat.exception.AppException;
import com.qchat.qchat.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(UUID id) {
        return userRepository.findById(id)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
