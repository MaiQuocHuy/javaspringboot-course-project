package project.ktc.springboot_app.auth.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import project.ktc.springboot_app.user.repositories.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {
	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository
				.findByEmailWithRoles(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	}
}
