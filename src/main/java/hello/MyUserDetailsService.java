package hello;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 21/11/14
 * Time: 13:43
 */

@Service//("userDetailsService")
public class MyUserDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Transactional(readOnly=true)
	@Override
	public UserDetails loadUserByUsername(final String username)
		throws UsernameNotFoundException {

		hello.User user = userRepository.findByUsername(username);
        if(user == null)
            user = new hello.User("", "", false);

		List<GrantedAuthority> authorities =
        buildUserAuthority(user.getUserRole());

		return buildUserForAuthentication(user, authorities);

	}
	private MediUser buildUserForAuthentication(hello.User user,
		List<GrantedAuthority> authorities) {
		return new MediUser(user.getUsername(), user.getPassword(),
			user.isEnabled(), true, true, true, authorities, user.getFullname());
	}

	private List<GrantedAuthority> buildUserAuthority(Set<UserRole> userRoles) {

		Set<GrantedAuthority> setAuths = new HashSet<GrantedAuthority>();

		// Build user's authorities
		for (UserRole userRole : userRoles) {
			setAuths.add(new SimpleGrantedAuthority(userRole.getRole()));
		}

		List<GrantedAuthority> Result = new ArrayList<GrantedAuthority>(setAuths);

		return Result;
	}

}

