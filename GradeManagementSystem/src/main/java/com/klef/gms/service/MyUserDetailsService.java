package com.klef.gms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.klef.gms.model.User;
import com.klef.gms.model.UserPrincipal;
import com.klef.gms.repo.UserRepo;


@Service
public class MyUserDetailsService implements UserDetailsService{

	@Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findByEmail(email)
                     .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // email as username
                .password(user.getPassword())
                .roles("USER") // or use user.getRole()
                .build();
    }

}
