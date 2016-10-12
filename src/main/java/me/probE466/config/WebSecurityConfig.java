package me.probE466.config;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;


//@EnableWebSecurity
//@Configuration
public class WebSecurityConfig  {

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//                .inMemoryAuthentication()
//                .withUser("user").password("password").roles("ADMIN");
    }

    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().ignoringAntMatchers("/post");
        http.authorizeRequests()
                .antMatchers("/admin/**")
                .authenticated()
                .antMatchers("/**").permitAll().and().httpBasic();
    }
}