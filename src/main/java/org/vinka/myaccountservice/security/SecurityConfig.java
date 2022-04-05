package org.vinka.myaccountservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.vinka.myaccountservice.business.service.UserService;

@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserService userDetailsService;
    private final AccessDeniedHandler accessDeniedHandler;

    @Autowired
    public SecurityConfig(
            UserService userDetailsService,
            AccessDeniedHandler handler
    ) {
        this.userDetailsService = userDetailsService;
        accessDeniedHandler = handler;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(encoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("actuator/**").permitAll()
                .mvcMatchers("api/empl/payment").hasAnyRole("USER", "ACCOUNTANT")
                .mvcMatchers("api/acct/payments").hasRole("ACCOUNTANT")
                .mvcMatchers("api/admin/**").hasRole("ADMINISTRATOR")
                .mvcMatchers(HttpMethod.DELETE, "api/admin/user/").hasRole("ADMINISTRATOR")
                .mvcMatchers("api/auth/changepass").authenticated()
                .mvcMatchers("api/security/events").hasRole("AUDITOR")
                .mvcMatchers("api/auth/signup").permitAll()
                .and().csrf().disable()
                .exceptionHandling().accessDeniedHandler(accessDeniedHandler)
                .and()
                .httpBasic().authenticationEntryPoint(authenticationEntryPoint());
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthEntryPoint();
    }

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(13);
    }
}
