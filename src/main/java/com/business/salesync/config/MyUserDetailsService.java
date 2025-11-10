package com.business.salesync.config;

import com.business.salesync.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {
   private UserRepository userRepository;

   public MyUserDetailsService(UserRepository userRepository) {
      this.userRepository = userRepository;
   }

   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
      UserDetails user = this.userRepository.findByUsername(username);
      if (user == null) {
         throw new UsernameNotFoundException(username);
      } else {
         return user;
      }
   }
}