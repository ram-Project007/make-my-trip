package com.makemytrip.makemytrip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.makemytrip.makemytrip.repositories.UserRepository;
@Configuration
public class SecurityConfig {
    @Bean public UserDetailsService userDetailsService(UserRepository users) { return email -> { com.makemytrip.makemytrip.models.Users u=users.findByEmail(email); if(u==null) throw new UsernameNotFoundException("User not found"); String role=u.getRole()==null?"USER":u.getRole().replaceFirst("^ROLE_","").toUpperCase(java.util.Locale.ROOT); return User.withUsername(u.getEmail()).password(u.getPassword()).roles(role).build(); }; }
    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { http.csrf(c->c.disable()).cors(c->{}).httpBasic(h->{}).authorizeHttpRequests(a->a.requestMatchers("/","/user/login","/user/signup","/hotel","/flight","/travel/options","/travel/options/*","/api/reviews","/api/reviews/*").permitAll().requestMatchers("/admin/**","/api/admin/**").hasRole("ADMIN").requestMatchers(org.springframework.http.HttpMethod.POST,"/api/hotels/*/rooms").hasRole("ADMIN").anyRequest().authenticated()); return http.build(); }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Allow all endpoints
                        .allowedOrigins("*") // Allow all origins
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allow specific HTTP methods
                        .allowedHeaders("*") // Allow all headers
                        .allowCredentials(false); // Disallow credentials for safety
            }
        };
    }
}
